package otus.homework.coroutines

import kotlinx.coroutines.*
import java.net.SocketTimeoutException

class CatsPresenter(
    private val catsService: CatsService
) {
    private var _catsView: ICatsView? = null

    private var job: Job = Job()

    private val presenterScope: CoroutineScope =
        CoroutineScope(CoroutineName("CatsCoroutine") + Dispatchers.Main + job)

    fun onInitComplete() {
        presenterScope.launch {
            try {
                val fact = async(Dispatchers.IO) { catsService.getCatFact() }
                _catsView?.populate(fact.await())
            } catch (e: Exception) {
                when (e) {
                    is CancellationException -> throw e
                    is SocketTimeoutException -> _catsView?.toast("Не удалось получить ответ от сервером")
                    else -> {
                        _catsView?.toast(e.message.toString())
                        CrashMonitor.trackWarning()
                    }
                }
            }
        }
    }

    fun attachView(catsView: ICatsView) {
        _catsView = catsView
    }

    fun detachView() {
        job.cancel()
        _catsView = null
    }
}