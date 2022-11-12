package seifemadhamdy.locationreminders.locationreminders.reminderslist

import seifemadhamdy.locationreminders.R
import seifemadhamdy.locationreminders.base.BaseRecyclerViewAdapter

// Use data binding to show the reminder on the item
class ReminderListAdapter(callBack: (selectedReminder: ReminderDataItem) -> Unit) :
    BaseRecyclerViewAdapter<ReminderDataItem>(callBack) {

    override fun getLayoutRes(viewType: Int) = R.layout.it_reminder
}