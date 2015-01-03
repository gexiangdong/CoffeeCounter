package com.cvook.coffeecounter;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

/**
 * Implementation of App Widget functionality.
 */
public class CoffeeCounterAppWidget extends AppWidgetProvider {
    final static String ADDACUP_CLICKED_ACTION = "com.cvook.coffeecounter.CoffeeCounterAppWidget.ADDACUP_CLICKED_ACTION";
    final static String COUNTER_CLICKED_ACTION = "com.cvook.coffeecounter.CoffeeCounterAppWidget.COUNTER_CLICKED_ACTION";


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            updateAppWidget(context, appWidgetManager, appWidgetIds[i]);
        }
    }


    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("CC", "CoffeeCounterAppWidget.onReceive(...) " + intent.getAction());
        if(ADDACUP_CLICKED_ACTION.equals(intent.getAction())){
            //TODO: ADD A CUP.
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context,
                    CoffeeCounterAppWidget.class));
            onUpdate(context, appWidgetManager, appWidgetIds);
        }else if(COUNTER_CLICKED_ACTION.equals(intent.getAction())){
            //start the main activity
            Intent intentMain = new Intent(context, MainActivity.class);
            intentMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intentMain);
        }else{
            super.onReceive(context, intent);
        }
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        Log.d("CC", "CoffeeCounterAppWidget.updateAppWidget(...) " + appWidgetId);
        //TODO: retrieve today's counter

        CharSequence widgetText = String.valueOf(System.currentTimeMillis() % 10);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.coffee_counter_app_widget);
        views.setTextViewText(R.id.appwidget_today_cups, widgetText);


        Intent intentAddACup = new Intent(context, CoffeeCounterAppWidget.class);
        intentAddACup.setAction(ADDACUP_CLICKED_ACTION);
        PendingIntent pendingIntentAddACup = PendingIntent.getBroadcast(context, 0, intentAddACup, 0);
        views.setOnClickPendingIntent(R.id.appwidget_addacup, pendingIntentAddACup);

        Intent intentCounter = new Intent(context, CoffeeCounterAppWidget.class);
        intentCounter.setAction(COUNTER_CLICKED_ACTION);
        PendingIntent pendingIntentCounter = PendingIntent.getBroadcast(context, 0, intentCounter, 0);
        views.setOnClickPendingIntent(R.id.appwidget_today_cups, pendingIntentCounter);


        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);    }
}


