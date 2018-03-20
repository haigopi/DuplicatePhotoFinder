import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;

public class Start {

	public static void main(String[] args) throws Exception {

		String readDir = "H:\\Photos";
		String duplicateDir = "H:\\Photos\\duplicatePhotos";
		String regularDir = "H:\\Photos\\regularPhotos";
		String movies = "H:\\Photos\\movies";

		ReadPhotosFrom r = new ReadPhotosFrom(readDir, duplicateDir,
				regularDir, movies, true);

		r.startReading(new File(readDir));
		System.out.println("Total Count : " + r.totalCount);
	}

}

class ReadPhotosFrom {

	boolean toIncludeSubDirs;
	File duplicateDir = null;
	File regularDir = null;
	File moviesDir = null;
	public int totalCount = 0;
	
	public ReadPhotosFrom(String readDir, String duplicateDir,
			String regularDir, String movies, boolean toIncludeSubDirs) {

		this.duplicateDir = new File(duplicateDir);
		this.regularDir = new File(regularDir);
		this.toIncludeSubDirs = toIncludeSubDirs;
		this.moviesDir = new File(movies);
	}

	public void startReading(File readDir) throws Exception {
		
		File[] allFiles = readDir.listFiles();
		System.out.println("Looking inside "+ readDir + " - Number of files found "+allFiles.length);
		WritePhotosTo duplicates = new WriteDuplicatePhotosTo(duplicateDir);
		WritePhotosTo regular = new WriteRegularPhotosTo(regularDir);
		WritePhotosTo movies = new WriteRegularMoviesTo(moviesDir);
		File dup;
		int count = 0;
		int duplicatesCount = 0;
		for (File ef : allFiles) {

			if (ef.isDirectory() && toIncludeSubDirs) {
				//System.out.println("Directory found : " + ef.getAbsolutePath() + " Now processing...this ");
				startReading(ef);
			}

			if (ef.isDirectory()) {
				System.out.println("Directory found as file: Ignoring" );
				continue;
			}

			
			if (ef.getName().endsWith("db") || ef.getName().endsWith("jar") || ef.getName().contains(" - Copy")) {
				System.out.println("Deleting : "+ ef.getName());
				ef.delete();
				continue;
			}
			
			long size = FileUtils.sizeOf(ef);
			String name = ef.getName();
			long time = ef.lastModified();
			Path path;
			
			if(ef.getName().endsWith("MOV") || ef.getName().endsWith("mov"))
				path = Paths.get(moviesDir.getAbsolutePath() +"\\" +name);
			else
				path = Paths.get(regularDir.getAbsolutePath() +"\\" +name);
		
			//System.out.println( (++totalCount) +"] Processing from "+ ef.getAbsolutePath() +" To : " +path);
			if (Files.exists(path)
					&& Files.size(path) == size
					&& Files.getLastModifiedTime(path).to(TimeUnit.MILLISECONDS) == time) {
				System.out.println(duplicatesCount +"] Duplicate : " + ef.getCanonicalPath());
				duplicates.saveFile(ef);
				//ef.delete();
				++duplicatesCount;
			} else if (Files.notExists(path) && !ef.getName().endsWith("MOV")) {
				//System.out.println(count + "] Not a duplicate : " + ef.getCanonicalPath());
				regular.saveFile(ef);
			}
			else if (Files.notExists(path) && ef.getName().endsWith("MOV")) {
				//System.out.println(count +"] Movie Copying to : " + ef.getCanonicalPath());
				movies.saveFile(ef);
			}
			++count;
		}
		System.out.println("Total File Processed inside " + readDir.getAbsolutePath()+ " : "+count);
		System.out.println("Total Duplicates Found inside " + readDir.getAbsolutePath()+ " : "+ duplicatesCount);
		
		
	}
	
}

abstract class WritePhotosTo {

	File f;

	public WritePhotosTo(File dir) throws Exception{
		this.f = dir;
		//System.out.println(f.getCanonicalPath() + " --> exists --> " +f.exists());
		if (f!=null && !f.exists())
		{
			System.out.println("Dir Not found -- So creating now");
			f.mkdir();
		}
		
	}

	public void saveFile(File ef) throws IOException {
		if(ef.isDirectory()){
			System.out.println("ERROR");
		}
		System.out.println("Copying File "+ef.getCanonicalPath()+ " to "+ f.getCanonicalPath());
		FileUtils.copyFileToDirectory(ef, f);

	}

}

class WriteDuplicatePhotosTo extends WritePhotosTo {

	public WriteDuplicatePhotosTo(File dir)  throws Exception{
		super(dir);
	}

}


class WriteRegularMoviesTo extends WritePhotosTo {

	public WriteRegularMoviesTo(File dir)  throws Exception{
		super(dir);
	}

}

class WriteRegularPhotosTo extends WritePhotosTo {

	public WriteRegularPhotosTo(File dir)  throws Exception{
		super(dir);
	}

}