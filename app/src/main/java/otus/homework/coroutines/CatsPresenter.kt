package otus.homework.coroutines

import kotlinx.coroutines.*
import retrofit2.Response
import java.net.SocketTimeoutException

class CatsPresenter(
    private val catsService: CatsService,
    private val catsServiceImg: CatsServiceImg
) {

    private var _catsView: ICatsView? = null
    private val presenterScope = PresenterScope()

    fun onInitComplete() {

        presenterScope.launch {
            try {
                val response =
                    async(Dispatchers.Default) {
                        catsService.getCatFact()
                    }

                val responseImg = async(Dispatchers.Default) {
                    catsServiceImg.getCatImage()
                }

                val resFact = response.await()
                val resImage = responseImg.await()

                withContext(Dispatchers.Main) {
                    if (checkResponse(resFact)) {
                        if (checkResponse(resImage)) {
                            _catsView?.populate(
                                CatsData(
                                    resFact.body()!!.text,
                                    resImage.body()!!.file
                                )
                            )
                        } else {
                            CrashMonitor.trackWarning()
                        }
                    } else {
                        CrashMonitor.trackWarning()
                    }
                }
            } catch (e: Exception) {
                when (e) {
                    is CancellationException -> throw e
                    is SocketTimeoutException -> _catsView?.networkError()
                    else -> {
                        CrashMonitor.logException(e)
                        e.message?.let {
                            _catsView?.showToast(it)
                        }
                    }
                }
            }
        }
    }

    fun attachView(catsView: ICatsView) {
        _catsView = catsView
    }

    fun detachView() {
        _catsView = null
        presenterScope.cancel()
    }

    private fun checkResponse(response: Response<*>): Boolean {
        return response.isSuccessful && response.body() != null
    }
}