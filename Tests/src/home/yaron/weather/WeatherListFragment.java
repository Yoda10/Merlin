package home.yaron.weather;

import home.yaron.testsApp.R;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class WeatherListFragment extends Fragment implements OnClickListener
{
	private ListView listView;
	private View fragmentView;
	private WeatherForcastData weatherForcastData;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		// Inflate the layout for this fragment
		fragmentView = inflater.inflate(R.layout.fragment_weather_list, container, false);
		listView = (ListView)fragmentView.findViewById(R.id.fragment_weather_listView);
		listView.requestFocus();		

		// Check for wide layout w400dp = layout_h or smaller layout = layout_v.
		View vLayout = fragmentView.findViewById(R.id.fragment_weather_layout_v);
		if( vLayout != null )
		{
			RelativeLayout r1 = (RelativeLayout)fragmentView.findViewById(R.id.fragment_weather_layout_v_r1);
			int h1 = r1.getLayoutParams().height;

			RelativeLayout r2 = (RelativeLayout)fragmentView.findViewById(R.id.fragment_weather_layout_v_r2);
			int h2 = r2.getLayoutParams().height;

			int height = getResources().getDisplayMetrics().heightPixels;
			LinearLayout.LayoutParams lp3 = new LinearLayout.LayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, height - h1 - h2));
			listView.setLayoutParams(lp3);
		}	

		// Set list view.
		GsonHelper gsonHelper = new GsonHelper();
		weatherForcastData = gsonHelper.loadWeatherData(getActivity(),GsonHelper.JSON_FILE_NAME); // parse the json string from file.

		if( weatherForcastData != null )
			setListAdapterAndHeader();

		//		WeatherAdapter adapter = new WeatherAdapter(this.getActivity().getApplicationContext(),
		//				weatherForcastData.getWeatherList(),
		//				R.layout.weather_item,
		//				new String[] {WeatherForcast.MAX},
		//				new int[] { R.id.item_weather_name },
		//				weatherForcastData.getAverage());
		//
		//		listView.setAdapter(adapter);

		//		TextView cityView = (TextView)fragmentView.findViewById(R.id.fragment_weather_city);
		//		cityView.setText(weatherForcastData.getCityName());
		//		//setDatesToViews(fragmentView);		

		// Search button.
		Button searchButton = (Button)fragmentView.findViewById(R.id.fragment_weather_search_button);		
		searchButton.setOnClickListener(this);	
		//		int w = searchButton.getWidth();
		//		int h = searchButton.getHeight();

		return fragmentView;
	}

	private void setListAdapterAndHeader()
	{
		if( getActivity() == null || fragmentView == null ||
				weatherForcastData == null || listView == null ) return;

		// ---- Set list adapter ----

		WeatherAdapter adapter = new WeatherAdapter(getActivity().getApplicationContext(),
				weatherForcastData.getWeatherList(),
				R.layout.weather_item,
				new String[] {WeatherForcast.MAX},
				new int[] { R.id.item_weather_sun },
				weatherForcastData.getAverage());

		listView.setAdapter(adapter);	

		// ---- Set header views ----

		// Set city
		TextView cityView = (TextView)fragmentView.findViewById(R.id.fragment_weather_city);
		cityView.setText(weatherForcastData.getCityName());		

		// Set start and end dates
		TextView dateFromView = (TextView)fragmentView.findViewById(R.id.fragment_weather_date_from);				
		dateFromView.setText(weatherForcastData.getStartDate());
		TextView dateToView = (TextView)fragmentView.findViewById(R.id.fragment_weather_date_to);				
		dateToView.setText(weatherForcastData.getEndDate());
	}

	//	private void setDatesToViews(View fragmentView)
	//	{
	//		// Set dates		
	//		TextView dateFromView = (TextView)fragmentView.findViewById(R.id.fragment_weather_date_from);				
	//		dateFromView.setText(weatherForcastData.getStartDate());
	//		TextView dateToView = (TextView)fragmentView.findViewById(R.id.fragment_weather_date_to);				
	//		dateToView.setText(weatherForcastData.getEndDate());
	//	}

	private class JsonAsyncLoad extends AsyncTask<URL, Void, WeatherForcastData>
	{
		//private Activity contextActivity;
		private Button button;
		private ProgressBar progressBar;

		//		JsonAsyncLoad(Activity activity)
		//		{
		//			contextActivity = activity;
		//		}

		@Override
		protected void onPreExecute()
		{		
			super.onPreExecute();			

			if( fragmentView != null )
			{
				// Replace button with a progress bar.
				button = (Button)fragmentView.findViewById(R.id.fragment_weather_search_button);			
				final LayoutParams params = button.getLayoutParams();	
				final ViewGroup viewGroup = (ViewGroup)button.getParent();
				viewGroup.removeView(button);
				progressBar = new ProgressBar(getActivity());		
				viewGroup.addView(progressBar,1,params);
			}
		}	

		protected WeatherForcastData doInBackground(URL... urls)
		{
			GsonHelper gsonHelper = new GsonHelper();			
			return gsonHelper.loadWeatherData(getActivity().getBaseContext(), urls[0]);			
		}		

		//	@Override
		//	protected void onProgressUpdate(Void... values)
		//	{		
		//		super.onProgressUpdate(values);
		//	}

		@Override
		protected void onPostExecute(WeatherForcastData result)
		{	
			if( getActivity() == null ) return;
			
			if( result != null && fragmentView != null )
			{				
				weatherForcastData = result;
				setListAdapterAndHeader();
				button.setText(getResources().getString(R.string.button_search));
				fragmentView.invalidate();
			}
			else
				button.setText(R.string.button_search_problem);

			if( progressBar != null && button != null )
			{
				// Replace back the progress bar with the button.
				final ViewGroup viewGroup = (ViewGroup)progressBar.getParent();
				viewGroup.removeView(progressBar);				
				viewGroup.addView(button, 1);				
			}
		}

		@Override
		protected void onCancelled()
		{				
			super.onCancelled();
			//			if( jsonProgressDialog != null && jsonProgressDialog.isShowing() )
			//				jsonProgressDialog.cancel();

			//			if( jsonHandler != null )
			//				jsonHandler.abortHttpRequest();

			//contextActivity = null;
		}
	}

	@Override
	public void onClick(View v)
	{
		EditText searchCity = (EditText)fragmentView.findViewById(R.id.fragment_weather_search_city);
		String city = searchCity.getText().toString();

		if( !city.equalsIgnoreCase("update") )
		{
			URL url = setCityNameToUrl(city);	
			new JsonAsyncLoad().execute(url);
		}
		else
			updateListItem(); // Demo like updating the adapter data from the server.
	}

	/**
	 * Only for tests !!! Demo
	 */
	private void updateListItem()
	{
		// Change data.
		List<Map<String, Object>> list =(List<Map<String, Object>>)weatherForcastData.getWeatherList();
		list.get(2).put(WeatherForcast.MAX,19.4F); // for debug only.

		// Notify adapter.
		WeatherAdapter adapter = (WeatherAdapter)listView.getAdapter();
		adapter.notifyDataSetChanged();		
	}

	private URL setCityNameToUrl(String city)
	{
		URL weatherUrl = null;

		try
		{
			String urlString = TryActivity2.URL_WEATHER_FORCAST.replace("MyCity", city.trim());
			weatherUrl = new URL(urlString);		
		}
		catch(MalformedURLException e)
		{			
			e.printStackTrace();
			Toast toast = Toast.makeText(getActivity().getApplicationContext(), "City text is invalid.",Toast.LENGTH_LONG);
			toast.show();
		}	

		return weatherUrl;
	}
}