package brap.player;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

import brap.tool.Utils;

public class SampleClient {
	public static void main(String args[]) throws UnsupportedEncodingException {
		String fileName="";
		if(args.length>0){
			fileName=args[0];
		}else{
			//fileName="output/1_hotels.csv";
			//fileName="output/etsy.int";
			//fileName="output/yahoo.int";
			fileName="output/expedia.int";


		}
		
		List<String> lines = Utils.readLines(fileName);
		String interactions="\n";
		for(String line:lines){
			interactions+=line+"\n";
		}
				
		String http_url = "http://localhost:4444/brap?q="+URLEncoder.encode(interactions,"UTF-8");
		System.out.println(http_url);
		URL url;
		try {
			url = new URL(http_url);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			print_content(con);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("The end");
	} 

	private static void print_content(HttpURLConnection con) {
		if (con != null) {

			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(
						con.getInputStream()));
				String input;
				while ((input = br.readLine()) != null) {
					//System.out.println(input);
				}
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	}
}
