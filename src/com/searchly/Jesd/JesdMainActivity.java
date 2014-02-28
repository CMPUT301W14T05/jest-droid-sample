package com.searchly.Jesd;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.searchly.jestdroid.DroidClientConfig;
import com.searchly.jestdroid.JestClientFactory;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.core.Index;
import io.searchbox.indices.CreateIndex;

public class JesdMainActivity extends Activity {

    final Context context = this;
    private JestClient jestClient;
    final String INDEX_NAME = "articles";
    private String indexName;
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        requestApiKey();

        Button button1 = (Button) findViewById(R.id.button1);
        button1.setOnClickListener(new IndexButtonListener());
        
        Button button2 = (Button) findViewById(R.id.button2);
        button2.setOnClickListener(new IndexArticleButtonListener());
    }

    @SuppressWarnings("deprecation")
	private void simpleAlert(String title, String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialog.show();
    }

    private void requestApiKey() {
        final String SEARCHLY_URL = "http://site:d87a47445dc808449dd78637d9031609@bombur-us-east-1.searchly.com";
        final String CMPUT_301_URL = "http://cmput301.softwareprocess.es:8080/testing/";
    	DroidClientConfig clientConfig = new DroidClientConfig.Builder(SEARCHLY_URL)
    																  .build();

    	JestClientFactory jestClientFactory = new JestClientFactory();
    	jestClientFactory.setDroidClientConfig(clientConfig);
    	jestClient = jestClientFactory.getObject();

    }
    
    public class IndexArticleButtonListener implements View.OnClickListener {
    	final String ARTICLE_NAME = "lotr";
		@Override
		public void onClick(View arg0) {
			new CreateArticleTask().execute(ARTICLE_NAME);
			
		}
    	
    }

    public class IndexButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            // get prompts.xml view
            LayoutInflater li = LayoutInflater.from(context);
            View promptsView = li.inflate(R.layout.prompts, null);

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
            alertDialogBuilder.setView(promptsView);

            ((TextView) promptsView.findViewById(R.id.textView1)).setText("Name for the new index:");

            final EditText userInput = (EditText) promptsView.findViewById(R.id.editTextDialogUserInput);

            // set dialog message
            alertDialogBuilder
                    .setCancelable(false)
                    .setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                	indexName = userInput.getText().toString();
                                    new CreateIndexTask().execute(indexName);
                                }
                            })
                    .setNegativeButton("Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });

            // create alert dialog
            AlertDialog alertDialog = alertDialogBuilder.create();

            // show it
            alertDialog.show();
        }
    }

    class CreateIndexTask extends AsyncTask<String, Void, JestResult> {

        private Exception exception;
        private JestResult result;

        protected JestResult doInBackground(String... indexName) {
            try {
                result = jestClient.execute(new CreateIndex.Builder(indexName[0]).build());
                return result;
            } catch (Exception e) {
                this.exception = e;
                return null;
            }
        }

        protected void onPostExecute(JestResult feed) {
            if (exception == null) {
                if (result.isSucceeded()) {
                    simpleAlert("Created index", result.getJsonString());
                } else {
                    simpleAlert("Could not create index", result.getJsonString());
                }
            } else {
                simpleAlert("Exception occurred", exception.getMessage());
            }
        }
    }
    
    class CreateArticleTask extends AsyncTask<String, Void, JestResult> {
    	
    	private Exception exception;
        private JestResult result;
		
		protected JestResult doInBackground(String... articleName) {
			try {
				DummyObject dummy = new DummyObject();
				dummy.setField1("cats");
				dummy.setField2("kittens");
				
				Gson gson = new Gson();
				
				String source = gson.toJson(dummy);
				
				Index index = new Index.Builder(source).index(indexName).type("dummy").build();
				result = jestClient.execute(index);
				return result;
			} catch (Exception e){
				this.exception = e;
				return null;
			}
		}
    	
		protected void onPostExecute(JestResult feed){
			if (exception == null) {
                if (result.isSucceeded()) {
                    simpleAlert("indexed article", result.getJsonString());
                } else {
                    simpleAlert("Could not index article", result.getJsonString());
                }
            } else {
                simpleAlert("Exception occurred", exception.getMessage());
            }
		}
    }
    
    class DummyObject{
    	private String field1;
    	private String field2;
    	
    	
    	public String getField1() {
			return field1;
		}


		public void setField1(String field1) {
			this.field1 = field1;
		}


		public String getField2() {
			return field2;
		}


		public void setField2(String field2) {
			this.field2 = field2;
		}


		public DummyObject(){
    		super();
    	}
    }
}
