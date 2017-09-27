package com.atsoft.thirukural.game;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DBController extends SQLiteOpenHelper {

	public DBController(Context context) {
		super(context, "Thirukural.db", null, 1);
		// TODO Auto-generated constructor stub
		
	}

	public DBController(Context context, String name, CursorFactory factory,
			int version, DatabaseErrorHandler errorHandler) {
		super(context, name, factory, version, errorHandler);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		// TODO Auto-generated method stub
		String query;
		query = "CREATE TABLE IF NOT EXISTS kural (kuralno TEXT, thirukural TEXT, mk_exp TEXT, varathu_exp TEXT, soloman_exp TEXT, parimelalagar_exp TEXT, manakadavure_exp TEXT, translate_kural TEXT, eng_exp TEXT)";
		database.execSQL(query);
		query = "CREATE TABLE IF NOT EXISTS allwords (words TEXT NOT NULL UNIQUE);";
		database.execSQL(query);
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub

	}
}
