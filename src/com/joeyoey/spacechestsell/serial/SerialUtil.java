package com.joeyoey.spacechestsell.serial;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import com.joeyoey.spacechestsell.objects.JoLocation;


public class SerialUtil {

	
	public static void writeToFile(HashMap<JoLocation, List<UUID>> input, File file) throws FileNotFoundException, IOException {
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
		
		out.writeObject(input);
		
		out.close();
	}
	
	
	@SuppressWarnings("unchecked")
	public static HashMap<JoLocation, List<UUID>> readFromFile(File file) throws FileNotFoundException, IOException, ClassNotFoundException {
		HashMap<JoLocation, List<UUID>> out = new HashMap<JoLocation, List<UUID>>();
		
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
		
		out = (HashMap<JoLocation, List<UUID>>) in.readObject();
		
		in.close();
		
		return out;
	}
	
	
}
