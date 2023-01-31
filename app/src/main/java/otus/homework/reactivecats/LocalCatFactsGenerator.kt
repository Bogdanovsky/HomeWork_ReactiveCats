package otus.homework.reactivecats

import android.content.Context
import io.reactivex.Flowable
import io.reactivex.Single
import java.util.concurrent.TimeUnit
import kotlin.random.Random
import kotlin.random.nextInt

class LocalCatFactsGenerator(
    private val resourceRepository: ContextResourceRepository
) {

    /**
     * Реализуйте функцию otus.homework.reactivecats.LocalCatFactsGenerator#generateCatFact так,
     * чтобы она возвращала Fact со случайной строкой  из массива строк R.array.local_cat_facts
     * обернутую в подходящий стрим(Flowable/Single/Observable и т.п)
     */
    fun generateCatFact(): Single<Fact> {
        return Single.fromCallable {
            val array = resourceRepository.getStringArray(R.array.local_cat_facts)
            val text = array[Random.nextInt(array.size)]
            Fact(text)
        }
    }

    /**
     * Реализуйте функцию otus.homework.reactivecats.LocalCatFactsGenerator#generateCatFactPeriodically так,
     * чтобы она эмитила Fact со случайной строкой из массива строк R.array.local_cat_facts каждые 2000 миллисекунд.
     * Если вновь заэмиченный Fact совпадает с предыдущим - пропускаем элемент.
     */
    fun generateCatFactPeriodically(): Flowable<Fact> {
        val success = Fact(resourceRepository.getStringArray(R.array.local_cat_facts)[Random.nextInt(5)])
        return Flowable.fromCallable { getRandomFact() }
            .delay(2000, TimeUnit.MILLISECONDS)
            .repeat()
            .distinctUntilChanged()
    }

    private fun getRandomFact() : Fact {
        val array = resourceRepository.getStringArray(R.array.local_cat_facts)
        val text = array[Random.nextInt(array.size)]
        return Fact(text)
    }
}