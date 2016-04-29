package org.irproject.movies;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.TreeSet;

import org.json.*;



public class Hits {
	
	public  Map<String, String> urlMap;
	public  Map<String, String> urlMapById;//id,url
	public  TreeMap<String, Integer> scoreMap=new TreeMap<>();
	public  Map<String, JSONObject> jsonMap=new HashMap<String,JSONObject>();
	public  List<String> inputUrls=new ArrayList<>();
	public  List<String> inputUrlId=new ArrayList<>();
	public  HashMap<String,ArrayList<String>> outLinks=new HashMap<>();
	public  HashMap<String,ArrayList<String>> inLinks=new HashMap<>();
	public  HashMap<String,ArrayList<String>> suboutLinks=new HashMap<>();
	public  HashMap<String,ArrayList<String>> subinLinks=new HashMap<>();
	public  TreeSet<String> unionUrlIds = new TreeSet<String>();
	public  HashMap<String,Double> hubScore=new HashMap<>();
    public  HashMap<String,Double> authScore=new HashMap<>();
    public  HashMap<String,Double> requiredAuthScore=new HashMap<>();
    public  Map<String, Double> sortedAuthScore;

	
	public  JSONArray getQueryResults(String  obj) throws JSONException{
		String toReturn=null;
		urlMap = Config.geturlMap();
		urlMapById= Config.geturlIdMap();
			
			try {				
				JSONArray jArray = new JSONArray(obj);
				
				int n=jArray.length();
				int i=0;
				while(i<10 && i<n){
					JSONObject jobj = jArray.getJSONObject(i);
					//System.out.println("##"+jobj.toString());
					String id = jobj.getString("id");
					jsonMap.put(id, jobj); //url, jsonobj
					inputUrls.add(id);
					i++;
					//id is the URL here					
				}
				
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			for(String s:inputUrls){
				String id=urlMap.get(s);
				
				inputUrlId.add(id);
				
			}
			//ArrayList<String> adjList = readTextFile("src/adjFile");
			makeGraph(Config.getAdjGraph());
			for(String key:inputUrlId){
				if(key==null) continue;
				unionUrlIds.add(key);
				ArrayList<String> outs=outLinks.get(key);	
				if(outs==null)
					continue;
				unionUrlIds.addAll(outs);				
				ArrayList<String> ins=inLinks.get(key);
				if(ins!=null)
				unionUrlIds.addAll(ins);
				
			}
			initializeRanking();
			compute();
			//toReturn=(getJSONObjectBack());
			
	/*	} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		return getJSONObjectBack();
	}

	
	private  JSONArray getJSONObjectBack() throws JSONException {
		//JSONObject json=new JSONObject();
		JSONArray jArray = new JSONArray();
		for(String s:sortedAuthScore.keySet()){
			String url=urlMapById.get(s);
			//System.out.println(url);
			//System.out.println("#####"+jsonMap.toString());
			jArray.put(jsonMap.get(url));
		}
		//json.put("hits", jArray);
		//System.out.println(json);
		return jArray;
	}

	public static <K extends Comparable,V extends Comparable> Map<K,V> sortByValues(Map<K,V> map){

		List<Map.Entry<K,V>> entries = new LinkedList<Map.Entry<K,V>>(map.entrySet());
	      
        Collections.sort(entries, new Comparator<Map.Entry<K,V>>() {

            @Override
            public int compare(Entry<K, V> o1, Entry<K, V> o2) {
                return o1.getValue().compareTo(o2.getValue());
            }
        });
      
        Map<K,V> sortedMap = new LinkedHashMap<K,V>();
      
        for(Map.Entry<K,V> entry: entries){
            sortedMap.put(entry.getKey(), entry.getValue());
        }
      
        return sortedMap;
		
	}

	public  void initializeRanking(){
	        
            double initialRank = 1;
            for(String key :unionUrlIds){
            	//System.out.println("unionUrlIds   - "+ key);
                    
                    hubScore.put(key, initialRank);
                    authScore.put(key, initialRank);
            }
	               
	 }
	public boolean isConverged(HashMap<String,Double> newRank,HashMap<String,Double> oldRank){
		boolean converged = true;;
		Double tolerance = (0.01d);
		for(String key : newRank.keySet()){
			Double a=(newRank.get(key));
			Double c=a-(oldRank.get(key));
			if((Math.abs(c))>(tolerance)){
				converged = false;
				break;
			}
		}
		return converged;
	}
	 public  void compute(){
	        
	        HashMap<String,Double> newAuthRank = calcAuthscore();
            HashMap<String,Double> newHubRank = calcHubScore();
	        while(!isConverged(newAuthRank, authScore)){
                  
                    newAuthRank = calcAuthscore();
                    newHubRank = calcHubScore();
                    hubScore = newHubRank;
                    authScore = newAuthRank;
            }
            for(String id:inputUrlId){
            	Double score=authScore.get(id);
            	
            	if(score==null)continue;
            	requiredAuthScore.put(id, score);
            }          
          
           
            double maxHubScore = 0.0;
            double maxAuthScore = 0.0;
            sortedAuthScore = sortByValues(requiredAuthScore);
            FileWriter hubScoreFile;
           /* try {
                    hubScoreFile = new FileWriter("src/authScore",true);
                    for(String key : sortedAuthScore.keySet()){
                            hubScoreFile.write(key+" "+sortedAuthScore.get(key)+"    "+urlMapById.get(key)+"\n");
                    }
                    hubScoreFile.close();


            } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
            }*/

	                

	 }
	 public  HashMap<String,Double> calcHubScore(){
         HashMap<String,Double> tempRank = new HashMap<>();
         double norm=0;
         for(String key :hubScore.keySet()){
                 tempRank.put(key, 0.0);
                 ArrayList<String> tempList = outLinks.get(key);
                 if(tempList==null){

                         continue;
                 }
                 double hubScore =0.0;
                 if(tempList!=null){
                         for(String dest : tempList){
                        	 if(unionUrlIds.contains(dest))
                                 hubScore+=authScore.get(dest);
                         }
                         norm+=Math.pow(hubScore, 2);
                 }
                 norm+=Math.pow(hubScore, 2);
                 tempRank.put(key, hubScore);
         }
         norm=Math.sqrt(norm);
         for(String key:tempRank.keySet()){
                 tempRank.put(key, (tempRank.get(key)/norm));
         }
         return tempRank;
 }
	 
	 public  HashMap<String,Double> calcAuthscore(){
         HashMap<String,Double> tempRank = new HashMap<>();
         double norm=0;
         for(String key :authScore.keySet()){
                 tempRank.put(key, 0.0);
                 ArrayList<String> tempList = inLinks.get(key);
                 if(tempList==null){

                         continue;
                 }
                 double authScore =0.0;
                 for(String dest : tempList){
                	 if(unionUrlIds.contains(dest))
                         authScore+=hubScore.get(dest);
                 }
                 norm+=Math.pow(authScore, 2);
                 tempRank.put(key, authScore);
         }
         norm=Math.sqrt(norm);
         for(String key:tempRank.keySet()){
                 tempRank.put(key, (tempRank.get(key)/norm));
         }
         return tempRank;
 }


	public  void makeGraph(ArrayList<String> adjList){
		for (String line : adjList) {
			String[] lineSplit = line.split(" ");
			String src = lineSplit[0].trim();
			String dest = lineSplit[1].trim();
			addToHashMap(outLinks, src, dest);
			addToHashMap(inLinks, dest, src);
            
		}
	}
	public  void addToHashMap(HashMap<String,ArrayList<String>> map,String key,String val){
		if(map.containsKey(key)){
			map.get(key).add(val);
		} else {
			ArrayList<String> tempList = new ArrayList<>();
			tempList.add(val);
			map.put(key, tempList);
		}
	}
		
	 public  ArrayList<String> readTextFile(String fileName){
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
	 
	 private static HashMap<String, Double> sortByValues(HashMap<String, Double> map) { 
	       List list = new LinkedList(map.entrySet());
	       // Defined Custom Comparator here
	       Collections.sort(list, new Comparator() {
	            public int compare(Object o1, Object o2) {
	               return ((Comparable) ((Map.Entry) (o2)).getValue())
	                  .compareTo(((Map.Entry) (o1)).getValue());
	            }
	       });

	       HashMap sortedHashMap = new LinkedHashMap();
	       for (Iterator it = list.iterator(); it.hasNext();) {
	              Map.Entry entry = (Map.Entry) it.next();
	              
	              sortedHashMap.put(entry.getKey(), entry.getValue());
	       } 
	       return sortedHashMap;
	  }

}

