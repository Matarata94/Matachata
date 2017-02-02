package matarata.ir.matachata;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DatabaseHandler extends SQLiteOpenHelper {
	
	private final Context mycontext;
	public final String path = "data/data/matarata.ir.matachata/databases/";
	public final String dbname = "chatuser_db.db";
	public SQLiteDatabase mydb;
	
	public DatabaseHandler(Context context) {
		super(context, "chatuser_db.db", null, 1);
		mycontext = context;
	}
	public void onCreate(SQLiteDatabase db) {
		
	}
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
	}
	public boolean checkdb(){
		SQLiteDatabase db = null;
		try{
		db = SQLiteDatabase.openDatabase(path + dbname, null, SQLiteDatabase.OPEN_READONLY);
		}catch(SQLException e)
		{
			
		}
		return db != null ? true:false;
	}
	public void databasecreate(){
		boolean checkdb = checkdb();
		if(checkdb){
			
		}else{
			this.getReadableDatabase();
			try{
			copydatabase();
			}catch(IOException e){
				
			}
		}
	}
	public void copydatabase() throws IOException{
		OutputStream myOutput = new FileOutputStream(path + dbname);
		byte[] buffer = new byte[1024];
		int length;
		InputStream myInput = mycontext.getAssets().open(dbname);
		while((length = myInput.read(buffer)) > 0){
			myOutput.write(buffer, 0, length);
		}
		myInput.close();
		myOutput.flush();
		myOutput.close();
	}
	public void open(){
		mydb = SQLiteDatabase.openDatabase(path + dbname, null, SQLiteDatabase.OPEN_READWRITE);
	}
	public void close(){
		mydb.close();
	}

	public String Query(int id,int fild){
		Cursor cu = mydb.rawQuery("SELECT * FROM userinfo WHERE id='" + id + "'", null);
		if(cu.getCount() > 0){
			cu.moveToFirst();
			String res = cu.getString(fild);
			cu.close();
			return res;
		}else{
			return "no result";
		}
	}
	public void Update(String value, int id,String row){
		ContentValues cv = new ContentValues();
		cv.put(row, value);
		mydb.update("userinfo", cv, "id='" + id + "'", null);
	}
	public Integer count(){
		Cursor cu = mydb.query("userinfo", null, null, null, null, null, null);
		int s = cu.getCount();
		return s;
	}
	public void delete(int id){
		mydb.delete("userinfo", "id='" + id+"'", null);
	}
}