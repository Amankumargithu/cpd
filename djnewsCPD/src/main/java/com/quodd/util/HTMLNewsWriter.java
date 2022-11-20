package com.quodd.util;

import static com.quodd.controller.DJController.cpdProperties;
import static com.quodd.controller.DJController.logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;

public class HTMLNewsWriter {
	private String mDatabaseDirectory = null;
	private String mTemplate = null;
	private String mHeadlineTag = null;
	private String mStoryTag = null;

	public HTMLNewsWriter(String databaseDirectory) {
		try {
			this.mDatabaseDirectory = databaseDirectory;
			File vFile = new File(this.mDatabaseDirectory);
			if (!vFile.exists()) {
				vFile.mkdirs();
			}
			this.mTemplate = cpdProperties.getStringProperty("TEMPLATE",
					"/home/djMysql/properties/dj_news_template.html");
			// Check if template file exists.
			vFile = new File(this.mTemplate);
			if (!vFile.isFile()) {
				logger.warning("Unable to find HTML template file " + this.mTemplate);
				logger.warning("Exiting System.");
				System.exit(0);
			}
			// Retrieve the headline tag in the HTML template file.
			this.mHeadlineTag = cpdProperties.getStringProperty("TEMPLATE_HEADING_TAG", "<!--HL-->");
			// Retrieve the story tag in the HTML template file.
			this.mStoryTag = cpdProperties.getStringProperty("TEMPLATE_STORY_TAG", "<!--STORY-->");
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
	}

	public String createHTMLStoryFile(String aHeadline, String aStory, String aBaseName, String vNewFilename) {
		if (aStory == null || aStory.isEmpty()) {
			logger.warning("parameter [aStory] was blank.");
			return null;
		}
		if (vNewFilename == null || vNewFilename.isEmpty()) {
			logger.warning("parameter [vNewFilename] was blank.");
			return null;
		}
		// Check that 'vNewFilename' value is a directory.
		File vFilePath = new File(vNewFilename);
		File vPath = new File(vFilePath.getParent());
		if (!vPath.isDirectory()) {
			logger.warning("parameter [vNewFilename] in not a valid directory.");
			return null;
		}
		boolean vTemplateOpened = false;
		try (BufferedReader vTemplateFile = new BufferedReader(new FileReader(this.mTemplate));
				PrintWriter vHTMLFile = new PrintWriter(new BufferedWriter(new FileWriter(vNewFilename)));) {
			vTemplateOpened = true;
			// Write from template file to the new HTML file.
			String vLine = null;
			while ((vLine = vTemplateFile.readLine()) != null) {
				// Check for headline tag.
				int vIndex = -1;
				if (aHeadline != null && !aHeadline.isEmpty() && this.mHeadlineTag != null
						&& !this.mHeadlineTag.isEmpty()) {
					vIndex = vLine.indexOf(this.mHeadlineTag);
					if (vIndex >= 0) {
						if (vIndex > 0)
							vHTMLFile.print(vLine.substring(0, vIndex));
						int vEndIndex = vIndex + this.mHeadlineTag.length();
						if (vEndIndex == vLine.length()) {
							vHTMLFile.println(aHeadline);
							continue;
						} else {
							vHTMLFile.print(aHeadline);
							vHTMLFile.println(vLine.substring(vEndIndex));
							continue;
						}
					}
				}
				if ((vIndex = vLine.indexOf(this.mStoryTag)) >= 0) {
					if (vIndex > 0)
						vHTMLFile.println(vLine.substring(0, vIndex));
					writeStory(aStory, vHTMLFile);
					int vEndIndex = vIndex + this.mStoryTag.length();
					if (vEndIndex < vLine.length())
						vHTMLFile.println(vLine.substring(vEndIndex));
				} else
					vHTMLFile.println(vLine);
			}
		} catch (FileNotFoundException e) {
			String vMsg = "HTMLNewsWriter.createHTMLStoryFile(): unable to ";
			// Template gets opened first.
			if (!vTemplateOpened)
				vMsg += "open file [" + this.mTemplate + "]. " + e.getMessage();
			else
				vMsg += "open file. " + e.getMessage();
			logger.log(Level.WARNING, vMsg, e);
			vNewFilename = null;
		} catch (IOException e) {
			logger.log(Level.WARNING, e.getMessage(), e);
			vNewFilename = null;
		}
		return vNewFilename;
	}

	private void writeStory(String aStory, PrintWriter aHTMLFile) {
		if (aStory == null || aStory.isEmpty()) {
			logger.warning("parameter [aStory] was blank.");
			return;
		}
		if (aHTMLFile == null) {
			logger.warning("parameter [aHTMLFile] was null.");
			return;
		}
		// Write the body of the story.
		int vIndex = 0;
		String vBreakTag = "<br/>";
		// Parse out all fields separated by comma.
		while (!aStory.isEmpty() && (vIndex = aStory.indexOf(vBreakTag)) >= 0) {
			if (vIndex > 0) {
				aHTMLFile.println(aStory.substring(0, vIndex));
			}
			if (vIndex == 0) {
				aHTMLFile.println("");
			}
			if (vIndex + vBreakTag.length() < aStory.length())
				aStory = aStory.substring(vIndex + vBreakTag.length());
			else
				aStory = "";
		}
		if (!aStory.isEmpty())
			aHTMLFile.println(aStory);
		else
			aHTMLFile.println("");
	}
}