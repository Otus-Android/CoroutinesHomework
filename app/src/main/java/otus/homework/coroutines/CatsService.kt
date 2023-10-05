package otus.homework.coroutines

import otus.homework.coroutines.model.Fact
import retrofit2.Call
import retrofit2.http.GET

interface CatsService {

    @GET("/fact")
    fun getCatFact() : Call<Fact>
}