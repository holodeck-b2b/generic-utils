/*******************************************************************************
 * Copyright (C) 2020 The Holodeck Team, Sander Fieten
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.holodeckb2b.commons.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import org.apache.tika.Tika;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;

/**
 * Provides some generic utility functions related to file handling.
 *
 * @author Sander Fieten (sander at holodeck-b2b.org)
 */
public final class FileUtils {

	/*
	 * Apache Tika mime type detector for detecting the mime type of payloads
	 */
	private static final class SingletonHolder {
		static final Tika mimeTypeDetector = new Tika();
	}

	/**
	 * Determines the mime type of the given file. It can not be guaranteed that this method will return the correct
	 * mime type. Therefore it is RECOMMENDED that the producer of the file supplies the [correct] mime type together
	 * with it.
	 * <p>
	 * Current implementation is based on <i>Apache Tika</i> which scans file contents to detect mime type.
	 *
	 * @param f Path of the file to determine the mime type for
	 * @return The detected mime type for the given file
	 * @throws IOException When the given file can not be accessed for mime type detection
	 */
	public static String detectMimeType(final String f) throws IOException {
		return detectMimeType(new File(f));
	}

	/**
	 * Determines the mime type of the given file. It can not be guaranteed that this method will return the correct
	 * mime type. Therefore it is RECOMMENDED that the producer of the file supplies the [correct] mime type together
	 * with it.
	 * <p>
	 * Current implementation is based on <i>Apache Tika</i> which scans file contents to detect mime type.
	 *
	 * @param f Path of the file to determine the mime type for
	 * @return The detected mime type for the given file
	 * @throws IOException When the given file can not be accessed for mime type detection
	 */
	public static String detectMimeType(final Path f) throws IOException {
		return detectMimeType(f.toFile());
	}

	/**
	 * Determines the mime type of the given file. It can not be guaranteed that this method will return the correct
	 * mime type. Therefore it is RECOMMENDED that the producer of the file supplies the [correct] mime type together
	 * with it.
	 * <p>
	 * Current implementation is based on <i>Apache Tika</i> which scans file contents to detect mime type.
	 *
	 * @param f The file to determine the mime type for
	 * @return The detected mime type for the given file
	 * @throws IOException When the given file can not be accessed for mime type detection
	 */
	public static String detectMimeType(final File f) throws IOException {
		return FileUtils.SingletonHolder.mimeTypeDetector.detect(f).toString();
	}

	/**
	 * Determines extension to use for the given mime type.
	 * <p>
	 * Current implementation is based on <i>Apache Tika</i>. If the given mime type is not recognized no extension will
	 * be returned
	 *
	 * @param mimeType The mime type to get the extension for
	 * @return The default extension for the given mime type<br>
	 *         or <code>null</code> when the mime type is not recognized
	 */
	public static String getExtension(final String mimeType) {
		if (Utils.isNullOrEmpty(mimeType))
			return null;

		final MimeTypes allTypes = MimeTypes.getDefaultMimeTypes();
		MimeType tikaMimeType = null;
		try {
			tikaMimeType = allTypes.forName(mimeType);
		} catch (final MimeTypeException ex) {
			// Can not detect the mime type of the given file, so no extension
			return null;
		}

		return tikaMimeType.getExtension();
	}

	/**
	 * Creates a new file with the given path if no file already exists with the same name. If there already exists a
	 * file with the same name a numerical suffix will be added to the file name until no duplicate exists. Also special
	 * characters contained in the file name are replaced by '_'.
	 * <p>
	 * <b>NOTE: </b>This method will create a new file. If the caller decides it doesn't need it anymore it is
	 * responsible for deleting it.
	 *
	 * @param baseName path of the file to be created.
	 * @return path to the new file.
	 * @throws IOException When the file could not be created, for example because a parent directory does not exist
	 * 						(anymore) or the parent directory is not writable.
	 */
	public static Path createFileWithUniqueName(final String baseName) throws IOException {
		return Utils.isNullOrEmpty(baseName) ? null : createFileWithUniqueName(Paths.get(baseName));
	}

	/**
	 * Creates a new file with the given path if no file already exists with the same name. If there already exists a
	 * file with the same name a numerical suffix will be added to the file name until no duplicate exists. Also special
	 * characters contained in the file name are replaced by '_'.
	 * <p>
	 * <b>NOTE: </b>This method will create a new file. If the caller decides it doesn't need it anymore it is
	 * responsible for deleting it.
	 *
	 * @param basePath path of the file to be created.
	 * @return path to the new file.
	 * @throws IOException When the file could not be created, for example because a parent directory does not exist
	 * 						(anymore) or the parent directory is not writable.
	 */
	public static Path createFileWithUniqueName(final Path basePath) throws IOException {
		if (basePath == null)
			return null;

		// Split the given path into name and extension part (if possible)
		String nameOnly = basePath.getFileName().toString();
		String ext = "";
		final int startExt = nameOnly.lastIndexOf(".");
		if (startExt > 0) {
			ext = nameOnly.substring(startExt);
			nameOnly = nameOnly.substring(0, startExt);
		}

		Path targetPath = basePath.getParent().resolve(nameOnly + ext);

		File f = null;
		int i = 1;
		while (f == null) {
			try {
				f = Files.createFile(targetPath).toFile();
			} catch (final FileAlreadyExistsException faee) {
				// Okay, the file already exists, try with increased sequence number
				targetPath = basePath.getParent().resolve(nameOnly + "-" + i++ + ext);
			}
		}

		return targetPath;
	}

	/**
	 * Checks if the specified path exists, is a directory and is writable.
	 *
	 * @param path The path to the directory to check
	 * @return <code>true</code> when the path is a writable directory,<br>
	 *         <code>false</code> if not
	 */
	public static boolean isWriteableDirectory(final String path) {
		try {
			return isWriteableDirectory(Paths.get(path));
		} catch (InvalidPathException e) {
			return false;
		}
	}

	/**
	 * Checks if the specified path exists, is a directory and is writable.
	 *
	 * @param p path to the directory to check
	 * @return <code>true</code> when the path is a writable directory,<br>
	 *         <code>false</code> if not
	 */
	public static boolean isWriteableDirectory(final Path p) {
		return p != null && Files.isDirectory(p) && Files.isWritable(p);
	}

	/**
	 * Removes a directory and all its content.
	 * This method will complete when the directory specified by the given path does not exist anymore.
	 *
	 * @param dir	path of the directory to remove, must not be <code>null</code>
	 * @throws IOException	if the directory and its content could not be removed completely. NOTE: When this exception
	 * 						is thrown the directory itself is not removed, but its content may be (partially) removed.
	 */
	public static void removeDirectory(Path dir)  throws IOException {
		if (dir == null)
			throw new IllegalArgumentException("Path to directory not specified");

		if (!Files.exists(dir) || !Files.isDirectory(dir))
			return;

		cleanDirectory(dir);

		if (!Files.deleteIfExists(dir))
			throw new IOException("Could not remove directory " + dir.toString());
	}

	/**
	 * Removes all content from a directory, this will both remove files and sub-directories.
	 *
	 * @param dir	path of the directory to clean, must not be <code>null</code>
	 * @throws FileNotFoundException if the specified directory does not exist
	 * @throws IOException	if some of the content could not be removed completely. NOTE: When this exception
	 * 						is thrown the content may be already been partially removed.
	 */
	public static void cleanDirectory(Path dir)  throws IOException {
		if (dir == null)
			throw new IllegalArgumentException("Path to directory not specified");

		if (!Files.exists(dir) || !Files.isDirectory(dir))
			throw new FileNotFoundException(dir.toString());

		for(File f : dir.toFile().listFiles()) {
			if (f.isDirectory())
				removeDirectory(f.toPath());
			else if (!f.delete())
				throw new IOException("Could not remove file " + f.toString());
		}
	}

	/**
	 * Sorts an array of files so that the filenames are in alphabetical order. The sort operational is done in the
	 * array itself so there is no return value.
	 *
	 * @param array The array to be sorted.
	 */
	public static void sortFiles(final File array[]) {
		if (array != null && array.length > 1)
			Arrays.sort(array, (File aO1, File aO2) -> aO1.getName().compareTo(aO2.getName()));
	}

	/**
	 * Replaces all special characters from the given file name with '_' and converts it to lower case.
	 * <p>
	 * Note: This method is very restrictive in the set of allowed characters, which is limited to all alpha numeric
	 * characters, '.', '-' and '_', to prevent any issue on different file systems.
	 *
	 * @param name file name to check
	 * @return converted version in lower case and with special characters replaced
	 */
	public static String sanitizeFileName(final String name) {
		return name.replaceAll("[^a-zA-Z0-9.\\-_]", "_").toLowerCase();
	}

	private FileUtils() {
	};
}
