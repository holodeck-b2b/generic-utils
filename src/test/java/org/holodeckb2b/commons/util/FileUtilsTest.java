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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Set;

import org.holodeckb2b.commons.testing.TestUtils;
import org.junit.jupiter.api.Test;

public class FileUtilsTest {

	@Test
	void testIsWriteableDir() {
		Path testDir = TestUtils.getTestResource("directory");			
		try {
			Files.createDirectory(testDir);
		} catch (IOException e) {
			fail(e);
		}
		
		Set<PosixFilePermission> permissions = new HashSet<>();
		permissions.add(PosixFilePermission.OWNER_READ);
		permissions.add(PosixFilePermission.OWNER_EXECUTE);
		try {
			Files.setPosixFilePermissions(testDir, permissions);
		} catch (IOException e) {
			fail(e);
		}		
		assertFalse(FileUtils.isWriteableDirectory(testDir));
		
		permissions.add(PosixFilePermission.OWNER_WRITE);
		try {
			Files.setPosixFilePermissions(testDir, permissions);
		} catch (IOException e) {
			fail(e);
		}		
		assertTrue(FileUtils.isWriteableDirectory(testDir));
		
		try {
			Files.delete(testDir);
		} catch (IOException e) {
			fail(e);
		}
	}
	
	
	@Test
	void testPreventDuplicateFileName() {
	    Path baseDir = TestUtils.getTestClassBasePath();
	            
	    try {
	        File dir = baseDir.toFile();
	        assertTrue(dir.isDirectory());
	        File[] files = dir.listFiles();
	        if(files.length == 0) {
	            new File(baseDir + "/emptyfile.xml").createNewFile();
	            new File(baseDir + "/emptyfile").createNewFile();
	            files = dir.listFiles();
	        }
	        String newFileName1 =
	                FileUtils.createFileWithUniqueName(baseDir + "/emptyfile.xml").toString();
	        String newFileName2 =
	                FileUtils.createFileWithUniqueName(baseDir + "/emptyfile").toString();
	        assertNotEquals(newFileName1, newFileName2);
	        for (File file : files) {
	            assertNotEquals(file.getAbsolutePath(), newFileName1);
	            assertNotEquals(file.getAbsolutePath(), newFileName2);
	        }
	
	        new File(newFileName1).delete();
	        new File(newFileName2).delete();   
	    } catch (IOException e) {
	    	e.printStackTrace();
	        fail();
	    }
	}
	
	@Test
	void testRemoveDirectory() {
		Path dirToDelete = TestUtils.getTestClassBasePath().resolve("dirtodelete");
		Path dirContent = TestUtils.getTestResource("dirtodelete_content");
		try {
			Files.walkFileTree(dirContent, 	new SimpleFileVisitor<Path>() {
				public FileVisitResult preVisitDirectory(Path p, BasicFileAttributes attr) throws IOException {
					Files.copy(p, dirToDelete.resolve(dirContent.relativize(p)));
					return FileVisitResult.CONTINUE;
				}
			
				public FileVisitResult visitFile(Path p, BasicFileAttributes attr) throws IOException {
					Files.copy(p, dirToDelete.resolve(dirContent.relativize(p)));						
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException e) {
			fail(e);
		}	
		
		assertDoesNotThrow(() -> FileUtils.removeDirectory(dirToDelete));		
		assertFalse(Files.exists(dirToDelete));
		
		Path fileNotToBeDeleted = TestUtils.getTestResource("dirtodelete_content/HelloWorld.txt");
		assertTrue(Files.exists(fileNotToBeDeleted));
		assertDoesNotThrow(() -> FileUtils.removeDirectory(fileNotToBeDeleted));
		assertTrue(Files.exists(fileNotToBeDeleted));	
		
		Path alreadyGone = TestUtils.getTestClassBasePath().resolve("alreadyGone");
		assertFalse(Files.exists(alreadyGone));
		
		assertDoesNotThrow(() -> FileUtils.removeDirectory(alreadyGone));
	}	

	@Test
	void testCleanDirectory() {
		Path dirToClean = TestUtils.getTestClassBasePath().resolve("dirtoclean");
		Path dirContent = TestUtils.getTestResource("dirtodelete_content");
		try {
			Files.walkFileTree(dirContent, 	new SimpleFileVisitor<Path>() {
				public FileVisitResult preVisitDirectory(Path p, BasicFileAttributes attr) throws IOException {
					Files.copy(p, dirToClean.resolve(dirContent.relativize(p)));
					return FileVisitResult.CONTINUE;
				}
				
				public FileVisitResult visitFile(Path p, BasicFileAttributes attr) throws IOException {
					Files.copy(p, dirToClean.resolve(dirContent.relativize(p)));						
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException e) {
			fail(e);
		}	
		
		assertDoesNotThrow(() -> FileUtils.cleanDirectory(dirToClean));		
		assertTrue(Files.exists(dirToClean));
		
		try {
			assertEquals(0, Files.list(dirToClean).count());
		} catch (IOException e) {
			fail(e);			
		}
	}	

}
