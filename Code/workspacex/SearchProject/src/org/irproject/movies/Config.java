package org.irproject.movies;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class Config implements ServletContextListener {
	public static Map<String, Integer> clustermap;
	public static Map<String,String> urlMap;
	public static Map<String,String> urlMapById;
	public static ArrayList<String> adjList;
	
	public static String clusterFileName = "C://Clusters.txt";
	public static String urlFileName = "C://urlMap";
	public static String adjFileName = "C://adjFile";

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {

	}

	@Override
	public void contextInitialized(ServletContextEvent event) {
		clustermap = new HashMap<String, Integer>();
		urlMap = new HashMap<String,String>();
		urlMapById = new HashMap<String,String>();
		
		InputStream input = event.getServletContext().getResourceAsStream(
				"/WebContent/cluster.txt");
		try {
			readFromFile(input);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			updateUrlMap(urlFileName);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		adjList = getAdjGraph(adjFileName);
		
		//System.out.println(urlMap.size());
		//System.out.println(adjList.toString());
		
		System.out.println("Initialized Cluster Map...");

	}

	private static void readFromFile(InputStream input) throws FileNotFoundException {
		Scanner in = new Scanner(new File(clusterFileName));
		String line;

		while (in.hasNextLine()) {
			line = in.nextLine();
			String[] words = line.split(",");

			if (clustermap.containsKey(words[0])) {
			} else {
				clustermap.put(words[0], Integer.valueOf(words[1]));

			}

		}

	}

	public void updateUrlMap(String urlFileName) throws NumberFormatException, IOException{
		FileReader fr=new FileReader(urlFileName);
		BufferedReader buffer = new BufferedReader(fr);
		String text=null;
		while((text=buffer.readLine())!=null){
			String parser[]=text.split("  ");
			int id=Integer.parseInt(parser[1]);
			urlMap.put( parser[0], parser[1]);//url, id
			urlMapById.put(parser[1], parser[0]);
			//System.out.println( parser[0]+"    "+parser[1]);
			
		}
	}
	
	public  ArrayList<String> getAdjGraph(String fileName){
        ArrayList<String> lineList = new ArrayList<String>();
        try{
                FileReader fileReader =
                     new FileReader(fileName);
                BufferedReader bufferedReader =
                     new BufferedReader(fileReader);
         String line =null;
         while((line = bufferedReader.readLine()) != null) {
             lineList.add(line);
         }
         bufferedReader.close();
         fileReader.close();
        } catch(Exception e){
                e.printStackTrace();
        }
        return lineList;
	}
	
	
	public static Map<String, Integer> getClusterMap() {
		return clustermap;
	}

	public static Map<String, String> geturlMap() {
		return urlMap;
	}
	public static Map<String, String> geturlIdMap() {
		return urlMapById;
	}
	public static ArrayList<String> getAdjGraph() {
		return adjList;
	}
}