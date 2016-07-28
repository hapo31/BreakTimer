package hapo31.jp.breaktimer

import android.accounts.AccountManager
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.CalendarContract
import android.util.Log
import android.view.Menu
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
//import kotlinx.android.synthetic.main.accounts_spinner.*
import java.text.SimpleDateFormat
import java.util.*

import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    val TAG : String = this.javaClass.simpleName

    var context : Context? = null
        get() = applicationContext

    val cr : ContentResolver by lazy {
        contentResolver
    }
    val adapter by lazy {
        ArrayAdapter<String>(context, R.layout.text_view)
    }

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
    val accountsSpinner by lazy {
        Spinner(context)
    }
    init {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        listView.adapter = adapter

        showCalendars("yasuhara@techfirm.co.jp")
        showCalendars("happo31@gmail.com")

        addEventButton.setOnClickListener()
        { v ->
            addEventCalendar()
            showCalendars("yasuhara@techfirm.co.jp")
            showCalendars("happo31@gmail.com")
        }

        accountSelectButton.setOnClickListener()
        { v ->
            val am = AccountManager.get(this)
            val accounts = am.getAccountsByType("com.google")
            if(accounts.size > 0) {

                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                for (account in accounts) {
                    adapter.add(account.name)
                    Log.v(TAG, "account:${account.name}")
                }
                accountsSpinner.adapter = adapter
                accountsSpinner.performClick()
            }
            else
            {
                Toast.makeText(context, "User is not found this device...", Toast.LENGTH_SHORT).show()

            }
        }

    }

    override fun onCreateOptionsMenu(menu : Menu) : Boolean {

        menu.add(Menu.NONE, 0, Menu.NONE, "アカウントの選択")

        return super.onCreateOptionsMenu(menu)
    }

    private fun addEventCalendar() : Unit
    {
        val values = ContentValues()
        values.put(CalendarContract.Events.CALENDAR_ID, 2)
        values.put(CalendarContract.Events.TITLE, "test")
        values.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
        //今から5分間のイベントを作成
        values.put(CalendarContract.Events.DTSTART, System.currentTimeMillis())
        values.put(CalendarContract.Events.DTEND, System.currentTimeMillis() + 1000 * 60 * 5)

        val uri = cr.insert(CalendarContract.Events.CONTENT_URI, values)
        val eventID = uri.lastPathSegment.toLong()
        Log.v(TAG, "EventID:$eventID")

        return
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

        adapter.add(account)

        val cursor = cr.query(CalendarContract.Events.CONTENT_URI, projection, selection, selectionArgs, null)

        var hasNext : Boolean = cursor?.moveToFirst()!!
        if(!hasNext)
        {
            adapter.add("予定なし")
            return
        }

        adapter.clear()

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
            adapter.add(result)

            hasNext = cursor?.moveToNext()!!
        }
        adapter.notifyDataSetChanged()
        cursor?.close()
    }

}
