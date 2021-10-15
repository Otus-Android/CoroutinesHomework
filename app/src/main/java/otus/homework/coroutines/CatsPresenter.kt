package otus.homework.coroutines

import android.util.Log
import kotlinx.coroutines.*
import java.lang.Exception
import kotlin.coroutines.CoroutineContext

class CatsPresenter(
    private val catsService: CatsService
) : CoroutineScope {

    override val coroutineContext: CoroutineContext = Dispatchers.Main + SupervisorJob() +
            CoroutineExceptionHandler { coroutineContext, throwable ->
                Log.d("Exaption", throwable.toString())
            } + CoroutineName("CatsCoroutine")


    private var _catsView: ICatsView? = null

    fun onInitComplete() {
        launch {
            try {
                val job = async(Dispatchers.IO) { catsService.getCatFact()}
                val jobImg = async(Dispatchers.IO) { catsService.getCatimg("https://aws.random.cat/meow")}
                _catsView?.populate(FullFact(job.await(),jobImg.await()))
            } catch (e: CancellationException) {
                Log.d("CoroutineExaption", e.toString())
            } catch (e: java.net.SocketTimeoutException) {
                _catsView?.callOnErrorSocketException()
            } catch (e: Exception) {
                CrashMonitor.trackWarning(e,TAG)
                _catsView?.callOnErrorAnyException(e)
            }
        }
    }

    fun attachView(catsView: ICatsView) {
        _catsView = catsView
    }

    fun detachView() {
        _catsView = null
        coroutineContext.cancelChildren()
    }

    companion object {
        val TAG = "CatsPresenter"
    }
}