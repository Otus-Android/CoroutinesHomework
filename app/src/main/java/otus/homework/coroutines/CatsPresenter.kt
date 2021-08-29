package otus.homework.coroutines

import kotlinx.coroutines.*
import retrofit2.Response
import java.net.SocketTimeoutException

class CatsPresenter(
    private val catsService: CatsService,
    private val imagesService: ImagesService
) {

    private var _catsView: ICatsView? = null

    private val presenterScope = CoroutineScope(Dispatchers.Main + CoroutineName("CatsCoroutine"))

    fun onInitComplete() {
        presenterScope.launch {
            try {
                val fact = presenterScope.async {getFact() }
                val image = presenterScope.async {getImg() }
                _catsView?.populate(Fact(fact.await()), Img(image.await()))

            } catch (e: Exception) {
                when (e) {
                    is SocketTimeoutException -> {
                       _catsView?.message("Не удалось получить ответ от сервером")
                    }
                    else -> {
                        CrashMonitor.trackWarning()
                    }
                }

            }
        }

    }

    private suspend fun getFact(): String {
        var res = catsService.getCatFact()
            if (res.isSuccessful && res.body() != null) {
                return res.body()!!.text
            }

        return ""
    }
    private suspend fun getImg(): String {
        var res = imagesService.getCatImg()
        if (res.isSuccessful && res.body() != null) {
            return res.body()!!.img
        }

        return ""
    }
    fun attachView(catsView: ICatsView) {
        _catsView = catsView
    }

    fun detachView() {
        _catsView = null
        presenterScope.cancel()
    }

}