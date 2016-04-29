import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;



import java.io.IOException;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;


public class converter {

	public static void main(String srgs[]) throws IOException
	{
		Directory dirIndex = FSDirectory.open(new File("E:/Spring2016/IR/Project/index1/index"));
        IndexReader reader = DirectoryReader.open(dirIndex);
        System.out.println(reader.numDocs());
        
        BufferedWriter bw = new BufferedWriter(new FileWriter("c"));
        for(int i = 1; i <= reader.numDocs(); ++i){
            String id = reader.document(i).get("id");
            String content = reader.document(i).get("content");
          id=id.replaceAll(",", "");
            content = (content.toLowerCase().replaceAll("\n", " ").replaceAll("[,=\\/]", " ").replaceAll("[^\\w\\s]", "")).replaceAll("\\d","").replaceAll("[+^:,?';=%#&~`$!@*_)/(}{]", "");
           
            String out = id+","+content+"\n";
           System.out.println(out);
           
           bw.write(out);
        
          }
         bw.close();
        reader.close();
	}
}
