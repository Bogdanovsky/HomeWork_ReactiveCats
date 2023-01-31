package otus.homework.reactivecats

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.io.IOException
import java.util.concurrent.TimeUnit

class CatsViewModel(
    catsService: CatsService,
    localCatFactsGenerator: LocalCatFactsGenerator,
    resourceRepository: ContextResourceRepository
) : ViewModel() {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData
    private val resourceRepository = resourceRepository
    private val catsService = catsService
    private val localCatFactsGenerator = localCatFactsGenerator

    private val disposables: MutableList<Disposable> = mutableListOf()

    init {
        getFacts()
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }

    private fun getFacts() {
        val disposable = catsService.getCatFact()
            .subscribeOn(Schedulers.io())
            .onErrorResumeNext(localCatFactsGenerator.generateCatFact())
            .delay(2000L, TimeUnit.MILLISECONDS)
            .repeat()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { fact ->
                    _catsLiveData.value = Success(fact)
                },
                { throwable ->
                    if (throwable is IOException) {
                        _catsLiveData.value = ServerError
                    } else {
                        _catsLiveData.value = Error(throwable.message
                            ?: resourceRepository.getString(R.string.default_error_text))
                    }
                }
            )
        disposables.add(disposable)

    }
}

class CatsViewModelFactory(
    private val catsRepository: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    private val resourceRepository: ContextResourceRepository
) :
    ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        CatsViewModel(catsRepository, localCatFactsGenerator, resourceRepository) as T
}

sealed class Result
data class Success(val fact: Fact) : Result()
data class Error(val message: String) : Result()
object ServerError : Result()