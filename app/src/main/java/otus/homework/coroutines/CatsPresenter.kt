package otus.homework.coroutines

import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.*
import java.net.SocketTimeoutException


class CatsPresenter(
    private val catsService: CatsService
) {

    private var _catsView: ICatsView? = null

    private val coroutineScope = CoroutineScope(Dispatchers.Main)
        .plus(CoroutineName("CatsCoroutine"))

    var errorResult: MutableLiveData<Result<CatResult>> = MutableLiveData()

    private val errorHandlerException: CoroutineExceptionHandler =
        CoroutineExceptionHandler { _, exception ->
            if (exception is SocketTimeoutException) {
                errorResult.postValue(Result.Error)
            } else {
                CrashMonitor.trackWarning()
            }
        }

    fun onInitComplete() {
        coroutineScope.launch(errorHandlerException) {
            val factDeferred = async { catsService.getCatFact() }
            val imageDeferred = async { catsService.getCatImage() }

            val fact = factDeferred.await()
            val image = imageDeferred.await()

            if (fact.isSuccessful && fact.body() != null) {
                throw IllegalStateException("Incorrect fact response: ${fact.message()}")
            }

            if (image.isSuccessful && image.body() != null) {
                throw IllegalStateException("Incorrect image response: ${image.message()}")
            }

            _catsView?.populate(CatResult(fact.body()!!, image.body()!!))
        }
    }

    fun attachView(catsView: ICatsView) {
        _catsView = catsView
    }

    fun detachView() {
        _catsView = null
    }

    fun cancel() {
        coroutineScope.cancel()
    }
}
