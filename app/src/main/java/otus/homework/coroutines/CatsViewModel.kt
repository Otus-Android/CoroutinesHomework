package otus.homework.coroutines

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class CatsViewModel(
    private val catsService: CatsFacstService,
    private val imageService: CatsImagesService
): ViewModel() {
    private val _uiStateLiveDate = MutableLiveData<CatsUiState>()

    val uiStateLiveData: LiveData<CatsUiState>
        get() = _uiStateLiveDate


    fun init(){
        _uiStateLiveDate.value = CatsUiState.Loading
        viewModelScope.launch(
            createExceptionHandler{error ->  handleError(error)}
        ){
            val facts = async { catsService.getCatFact() }
            val image = async { imageService.getImage() }

            _uiStateLiveDate.value =
                CatsUiState.Success(CatsContent(facts.await(), image.await().first()))
        }
    }

    private fun handleError(error: Throwable){
        if(error is java.net.SocketTimeoutException){
            _uiStateLiveDate.value =CatsUiState.Error(ERROR_MESSAGE)
        } else {
            CrashMonitor.trackWarning()
            _uiStateLiveDate.value = CatsUiState.Error(error.message.toString())
        }
    }

    private fun createExceptionHandler(errorBlock: (error: Throwable) -> Unit) =
        CoroutineExceptionHandler{ _, error ->
            errorBlock.invoke(error)
        }


    private companion object{
        const val ERROR_MESSAGE = "Не удалось получить ответ от серверa"
    }

    class CatsViewModelFactory(
        private val catsService: CatsFacstService,
        private val imageService: CatsImagesService
    ) :
        ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            CatsViewModel(catsService, imageService, ) as T
    }
}