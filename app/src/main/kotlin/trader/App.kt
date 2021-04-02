/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package trader

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.Connection
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

class App {
    companion object Scraper {
        fun getStocks(): Elements {
            val email: String? = System.getenv("EMAIL")
            val password: String? = System.getenv("PASSWORD")
            val response: Connection.Response = Jsoup.connect(Mfi.LOGIN.url).data("Email", email, "Password", password).method(Connection.Method.POST).execute()
            val cookies = response.cookies()

            val doc = Jsoup.connect(Mfi.SCREENER.url).method(Connection.Method.POST).data("MinimumMarketCap", "50", "Select30", "False").cookies(cookies).execute()
            return doc.parse().getElementsByClass("screeningdata").select("table")
        }

        fun getColumnHeaders(stocks: Elements): MutableList<String> {
            val dataHeader = stocks.select("th")
            val columnHeaders: MutableList<String> = mutableListOf()

            dataHeader.forEach {
                columnHeaders.add(it.text())
            }
            return columnHeaders
        }

        fun dataToMap(stocks: Elements, columnHeaders: MutableList<String>): MutableList<MutableMap<String, String>> {
            val fields: MutableList<String> = mutableListOf()
            val rows = stocks.select("tbody").select("tr")
            val stockOutput: MutableList<MutableMap<String, String>> = mutableListOf()

            for (row in rows) {
                var stockMap: MutableMap<String, String> = mutableMapOf()
                for ((index, value) in row.select("td").withIndex()) {
                    stockMap.put(columnHeaders[index], value.text())
                }
                stockOutput.add(stockMap)
            }

            return stockOutput
        }
    }

}

enum class Mfi(val url: String) {
    SCREENER("https://www.magicformulainvesting.com/Screening/StockScreening"),
    LOGIN("https://www.magicformulainvesting.com/Account/LogOn")
}


fun main() {
    val stocks = App.getStocks()
    val columnHeaders = App.getColumnHeaders(stocks)
    val stockList = App.dataToMap(stocks, columnHeaders)

    println(stockList)
}
