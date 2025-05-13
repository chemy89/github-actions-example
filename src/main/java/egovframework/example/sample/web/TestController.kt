package egovframework.example.sample.web

import egovframework.example.sample.service.EgovSampleService
import egovframework.example.sample.service.SampleVO
import org.egovframe.rte.fdl.property.EgovPropertyService
import org.egovframe.rte.psl.dataaccess.util.EgovMap
import org.egovframe.rte.ptl.mvc.tags.ui.pagination.PaginationInfo
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import java.text.DecimalFormat
import kotlin.math.ceil
import kotlin.random.Random

@Controller
class TestController(
    private val sampleService: EgovSampleService,
) {

    data class NewSampleVO(
        val id: String = "",
        val name: String,
        val description: String,
        val useYn: String,
        val regUser: String
    )

    data class TestItem(val value: String, val isBold: Boolean)

    private fun Int.toDecimalFormat() = DecimalFormat("###,###,###").format(this)
    private fun String.toPriceFormat() = "${this}원"

    private fun EgovMap.getStringValue(key: String) = this[key] as String

    @GetMapping("/test")
    @Throws(Exception::class)
    fun test(@ModelAttribute sampleVO: SampleVO, model: Model): String {
        println("test")

        val items = sampleService
            .selectSampleList(sampleVO)
            .map {
                val item = it as EgovMap
                NewSampleVO(
                    id = item.getStringValue("id"),
                    name = item.getStringValue("name"),
                    description = item.getStringValue("description"),
                    useYn = item.getStringValue("useYn"),
                    regUser = item.getStringValue("regUser"),
                )
            }

        val list = (1..20)
            .asSequence()
            .map { it * Random.nextInt(1, 10_001) }
            .map { ceil(it / 10.0).toInt() * 10 }
            .filter { it >= 10_000 }
            .map { TestItem(it.toDecimalFormat().toPriceFormat(), it > 100_000) }
            .toList()

        println(list)

        model.addAttribute("list", list)

        return "test"
    }

}