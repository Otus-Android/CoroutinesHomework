package otus.homework.coroutines

import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.lang.Exception

class CatsPresenter(
    private val catsService: CatsService
) {
    private val scope = PresenterScope()
    private var job: Job? = null

    private var _catsView: ICatsView? = null

    fun onInitComplete() {
        job = scope.launch {
            try {
                val fact = catsService.getCatFact()
                _catsView?.populate(fact)
            } catch (e: java.net.SocketTimeoutException) {
                _catsView?.showError("Не удалось получить ответ от сервером")
            } catch (e: Exception) {
                CrashMonitor.trackWarning()
                _catsView?.showError(e.message ?: "Unknown error")
            }
        }
    }

    fun attachView(catsView: ICatsView) {
        _catsView = catsView
    }

    fun detachView() {
        _catsView = null
        job?.cancel()
        job = null
    }
}