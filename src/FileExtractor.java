import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


public class FileExtractor {

	private final String prefix;
	private final File inputDirectory;
	private final File outputDirectory;
	
	private String call;
	
	
	public FileExtractor(String prefix, File zipFile, File outputDirectory) {
		this.prefix = prefix;
		this.inputDirectory = zipFile;
		this.outputDirectory = outputDirectory;
	}
	
	private void extractSubZipFile(File studentDirectory, ZipInputStream zipIn, ZipEntry entry) throws IOException {
		System.out.println(entry.getName());
		byte[] buff = new byte[(int) Math.max(entry.getSize(),1024)];
		zipIn.read(buff);
		
		ZipInputStream subZipIn = new ZipInputStream(new ByteArrayInputStream(buff));
		ZipEntry subEntry = subZipIn.getNextEntry();
		while (subEntry != null){
			
			byte[] subBuffer = new byte[(int) Math.max(subEntry.getSize(), 1024)];
			
			File outFile = new File(studentDirectory, subEntry.getName());
			File parent = outFile.getParentFile();
			if (!parent.exists()) {
				parent.mkdirs();
			}
			OutputStream out = new FileOutputStream(outFile);
			
			while (subZipIn.read(subBuffer) > 0) {
				out.write(subBuffer);
			}
			out.flush();
			out.close();
			
			subEntry = subZipIn.getNextEntry();
		}
	}
	
	private void extractZipFile(File studentFolder, File outputFolder) throws IOException {
		File submissionFolder = new File(studentFolder, "Submission attachment(s)");
		File[] submissions = submissionFolder.listFiles();
		for (File submission : submissions) {
			if (!submission.isDirectory() && submission.getName().endsWith(".zip")) {
				
				ZipInputStream zipIn = new ZipInputStream(new FileInputStream(submission));
				ZipEntry entry = zipIn.getNextEntry();
				while (entry != null){
					
					if (entry.getName().endsWith("/")) {

						entry = zipIn.getNextEntry();
						continue;
					}
					
					byte[] subBuffer = new byte[(int) Math.max(entry.getSize(), 1024)];
					
					File outFile = new File(outputFolder, entry.getName());
					File parent = outFile.getParentFile();
					if (!parent.exists()) {
						parent.mkdirs();
					}
					OutputStream out = new FileOutputStream(outFile);
					
					while (zipIn.read(subBuffer) > 0) {
						out.write(subBuffer);
					}
					out.flush();
					out.close();
					
					entry = zipIn.getNextEntry();
				}
			}
			break;
		}
	}
	
	private void extractStudentFiles(File studentFolder) throws IOException {
		String name = studentFolder.getName();		
		String studentName = name.substring(name.indexOf('(')+1, name.indexOf(')'));
		
		File outputStudentDirectory = new File (outputDirectory, studentName);
		outputStudentDirectory.mkdirs();
		call += " " + outputStudentDirectory.getAbsolutePath();
		extractZipFile(studentFolder, outputStudentDirectory);
	}
	
	
	public void extract() throws IOException {
		
		if (outputDirectory.exists()) {
			outputDirectory.delete();
		}
		outputDirectory.mkdirs();
		
		call = "java plag.parser.plaggie.Plaggie";
		
		File[] folders = inputDirectory.listFiles();
		for (File folder: folders) {
			if (folder.isDirectory()){
				extractStudentFiles(folder);
			}
		}
		
		System.out.println(call);
	}
	
	public static void main(String[] args) throws IOException {
		File inputDirectory = new File("/home/bartizzi/Workspaces/plagiarismDetector/Assignment2");
		File outputDirectory = new File("/home/bartizzi/Workspaces/plagiarismDetector/collectedAssignments/");
		FileExtractor extractor = new FileExtractor("Assignment2/", inputDirectory, outputDirectory);
		extractor.extract();
	}
}
