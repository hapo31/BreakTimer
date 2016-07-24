package hapo31.jp.breaktimer

import android.content.ContentResolver
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.CalendarContract
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    val TAG : String = this.javaClass.simpleName

    var context : Context? = null
    var cr : ContentResolver? = null
    var adapter : ArrayAdapter<String>? = null
    var listView : ListView? = null


    val projection = arrayOf(
            CalendarContract.Events._ID,
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DTSTART,
            CalendarContract.Events.DTEND
            )

    val selection = "( " +
            "(${CalendarContract.Events.ACCOUNT_NAME} = ?)" +
            " AND (${CalendarContract.Events.ACCOUNT_TYPE} = ?)" +
            " AND (${CalendarContract.Events.DTSTART} >= ?)" +
            " AND (${CalendarContract.Events.DTEND} <= ?)" +
            " AND (${CalendarContract.Events.CALENDAR_ID} = ?)" +
            ")"
    init {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        context = applicationContext
        adapter = ArrayAdapter<String>(context, R.layout.text_view)
        listView = findViewById(R.id.listView) as ListView
        listView?.adapter = adapter
        cr = contentResolver

        showCalendars("yasuhara@techfirm.co.jp")
        showCalendars("happo31@gmail.com")


    }


    private fun showCalendars(account : String) : Unit
    {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val monthStart = cal.timeInMillis
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        val monthEnd = cal.timeInMillis

        val selectionArgs = arrayOf(
                account,
                "com.google",
                monthStart.toString(),
                monthEnd.toString(),
                "2"
        )

        adapter?.add(account)

        val cursor = cr?.query(CalendarContract.Events.CONTENT_URI, projection, selection, selectionArgs, null)

        var hasNext : Boolean = cursor?.moveToFirst()!!
        if(!hasNext)
        {
            adapter?.add("予定なし")
            return
        }
        while(hasNext)
        {
            val id = cursor?.getLong(0)
            val title = cursor?.getString(1)
            val start = cursor?.getLong(2)
            val end = cursor?.getLong(3)

            val sdf = SimpleDateFormat("MM/dd hh:mm", Locale.JAPAN)

            val sDate = sdf.format(start)
            val eDate = sdf.format(end)
            val result = "$id $title:$sDate - $eDate"
            Log.v(TAG, result)
            adapter?.add(result)

            hasNext = cursor?.moveToNext()!!
        }
        adapter?.notifyDataSetChanged()
        cursor?.close()
    }

}
