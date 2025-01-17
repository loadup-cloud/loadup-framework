package com.github.loadup.components.gateway.plugin.repository.file.service.impl;

/*-
 * #%L
 * repository-file-plugin
 * %%
 * Copyright (C) 2022 - 2025 loadup_cloud
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import com.github.loadup.commons.error.CommonException;
import com.github.loadup.components.gateway.common.exception.util.AssertUtil;
import com.github.loadup.components.gateway.common.util.FileUtil;
import com.github.loadup.components.gateway.core.common.Constant;
import com.github.loadup.components.gateway.core.common.GatewayErrorCode;
import com.github.loadup.components.gateway.facade.util.LogUtil;
import com.github.loadup.components.gateway.plugin.repository.file.service.ConfigFileBuilder;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.jar.*;

/**
 * configuration file builder
 */
@Component
public class ConfigFileBuilderImpl implements ConfigFileBuilder {

	/**
	 * logger
	 */
	private final static Logger logger = LoggerFactory.getLogger(ConfigFileBuilderImpl.class);

	private final static String CLASS_JAR_FILE_SUFFIX = ".jar!";

	private final static String JAR_FILE_SUFFIX = ".jar";

	private final static String CLASSPATH_PREFIX = "classpath:";

	/**
	 * config file header validator
	 */
	@Override
	public boolean isValidHeaderFormat(List<String> fileRows) {
		//0. validate if empty file or only has 1 row as header
		if (CollectionUtils.isEmpty(fileRows) || fileRows.size() == 1) {
			//log sys error
			LogUtil.error(logger, "failed reading empty file or only header.");
			return false;
		}
		//1. read first line as column header if valid file
		String headerStr = fileRows.get(0);
		if (StringUtils.isBlank(headerStr)) {
			//log sys error
			LogUtil.error(logger, "The first row should be header and not blank.");
			return false;
		}
		//TODO add more validation for header
		//4. isValidHeaderFormat successfully
		return true;
	}

	/**
	 * read content of all files in a directory to string
	 */
	@Override
	public Map<String, String> readToStringForDirectory(File file) {
		Map<String, String> fileContentMap = new HashMap<String, String>();
		//if file exists
		if (file.exists()) {
			// check if it's a directory
			if (file.isDirectory()) {
				File[] files = file.listFiles();

				//validate empty file directory
				if (null == files || files.length == 0) {
					//log warn empty directory
					LogUtil.info(logger, "failed reading empty file=", file.getName());
					return fileContentMap;
				}
				for (File tempFile : files) {
					if (tempFile.isDirectory()) {
						readToStringForDirectory(tempFile);
					} else {
						fileContentMap.put(tempFile.getName(), readToString(tempFile));
					}
				}
			} else {
				fileContentMap.put(file.getName(), readToString(file));
			}
		}
		return fileContentMap;
	}

	/**
	 * read content of all files in a directory to string
	 */
	@Override
	public Map<String, String> readToStringForDirectory(String fileRootPath, String directory) {

		String configPath = buildFilePath(fileRootPath, directory);
		Map<String, String> resultMap = new HashMap<String, String>();
		//read config from current path
		resultMap = readToStringForDirectory(new File(configPath));

		//read config from jar
		if (MapUtils.isEmpty(resultMap)) {
			LogUtil.info(logger, "There is not path in path=", configPath);
			resultMap = readToStringForJar(fileRootPath, directory);
		}

		//read config from embedded jar
		if (MapUtils.isEmpty(resultMap)) {
			LogUtil.info(logger, "There is not path in jar=", fileRootPath);
			resultMap = readToStringForJar(fileRootPath, directory, resultMap);
		}

		return resultMap;
	}

	private Map<String, String> readToStringForJar(String fileRootPath, String directory) {
		Map<String, String> resultMap = new HashMap<String, String>();
		String jarFilePath = getJarFile(fileRootPath);
		List<String> fileList = getFilenameList(jarFilePath, directory);
		fileList.forEach(filePath -> {
			try {
				String fileName = getFileName(filePath);
				String content = FileUtil.getFileContent(filePath, this);
				resultMap.put(fileName, content);
			} catch (Exception e) {
				LogUtil.error(logger, e, "failed reading file in root path=" + fileRootPath + ", file path=" + directory);
			}
		});
		return resultMap;
	}

	private Map<String, String> readToStringForJar(String fileRootPath, String directory, Map<String, String> map) {
		try (JarFile firstJar = new JarFile(getJarFile(fileRootPath))) {

			for (Enumeration<JarEntry> e = firstJar.entries(); e.hasMoreElements(); ) {
				JarEntry jarEntry = e.nextElement();
				String jarEntryName = jarEntry.getName();

				if (StringUtils.contains(jarEntryName, JAR_FILE_SUFFIX)) {
					JarInputStream jisJar2 = new JarInputStream(firstJar.getInputStream(jarEntry));
					boolean inLoop = true;
					do {
						JarEntry entry = jisJar2.getNextJarEntry();
						if (null == entry) {
							inLoop = false;
						} else if (StringUtils.contains(entry.getName(), directory) && !StringUtils.endsWith(entry.getName(),
								File.separator)) {
							String entryName = entry.getName();
							InputStream inputFileStream = this.getClass().getClassLoader().getResourceAsStream(
									CLASSPATH_PREFIX.concat(entryName));
							StringWriter writer = new StringWriter();
							IOUtils.copy(inputFileStream, writer, StandardCharsets.UTF_8.name());
							String fileName = StringUtils.substring(entryName, StringUtils.lastIndexOf(entryName, File.separator) + 1);
							map.put(fileName, writer.toString());
						}
					} while (inLoop);
				}
			}

		} catch (IOException e) {
			LogUtil.error(logger, e, "failed reading file in root path=" + fileRootPath + ", file path=" + directory);
		}

		return map;
	}

	/**
	 * read content from file to string
	 */
	@Override
	public String readToString(File file) {
		String fileContent = "";
		try {
			fileContent = FileUtils.readFileToString(file);
		} catch (IOException e) {
			//log warn
			LogUtil.error(logger, e, "failed reading file=" + file.getName());
		}
		return fileContent;
	}

	/**
	 * read content from file to string list
	 */
	@Override
	public List<String> readLines(File file) throws IOException {
		return FileUtils.readLines(file);
	}

	/**
	 * read content of all files in a directory to string
	 */
	@Override
	public List<String> readToStringList(String fileRootPath, String filePath) {
		String configPath = buildFilePath(fileRootPath, filePath);
		LogUtil.info(logger, "config path is ", configPath);
		File configFile = new File(configPath);
		List<String> lines = new ArrayList<>();

		try {
			if (configFile.exists()) {
				LogUtil.info(logger, "read lines from file:", configPath);
				lines = readLines(configFile);
			} else {
				lines = readToStringList(filePath);
			}
		} catch (IOException e) {
			LogUtil.error(logger, e, "failed loading configuration file=" + filePath);
			throw new CommonException(GatewayErrorCode.SYSTEM_ERROR);
		} catch (CommonException e) {
			LogUtil.error(logger, e, "failed loading configuration file=" + filePath);
			throw e;
		} catch (Exception e) {
			LogUtil.error(logger, e, "failed loading configuration file=" + filePath);
			throw new CommonException(GatewayErrorCode.SYSTEM_ERROR);
		}

		return lines;
	}

	/**
	 * build real file path
	 */
	@Override
	public String buildFilePath(String fileRootPath, String filePath) {
		if (StringUtils.isBlank(fileRootPath)) {
			URL url = buildCurrentUrl(filePath);
			File file = new File(url.getPath());
			if (file.exists()) {
				return url.getPath();
			} else {
				URL uri = this.getClass().getResource("/");
				return uri.getPath().concat(filePath);
			}
		} else {
			return fileRootPath.concat(filePath);
		}
	}

	private List<String> readToStringList(String filePath) throws IOException {
		List<String> lines = new ArrayList<String>();
		URL url = buildCurrentUrl(filePath);
		if (url == null) {
			return lines;
		}

		String jarPath = getJarFile(url.getPath());
		try (JarFile jarFilePath = new JarFile(jarPath)) {
			Enumeration<JarEntry> entries = jarFilePath.entries();

			while (entries.hasMoreElements()) {
				JarEntry jarEntry = entries.nextElement();
				if (jarEntry.getName().contains(filePath)) {
					InputStream inputFileStream = null;
					BufferedReader bufferedReader = null;
					try {
						inputFileStream = jarFilePath.getInputStream(jarEntry);
						bufferedReader = new BufferedReader(new InputStreamReader(inputFileStream));
						bufferedReader.lines().forEach(line -> {
							lines.add(line);
						});
					} catch (Exception e) {
						LogUtil.error(logger, e, "Invalid file path, filePath=", filePath);
					} finally {
						if (null != inputFileStream) {
							inputFileStream.close();
						}
						if (null != bufferedReader) {
							bufferedReader.close();
						}
						break;
					}
				}
			}

			if (CollectionUtils.isEmpty(lines)) {
				return readToStringListFromClassPath(filePath);
			}
		}
		return lines;
	}

	private URL buildCurrentUrl(String filePath) {
		URL url = this.getClass().getClassLoader().getResource(filePath);
		if (null == url) {
			LogUtil.error(logger, "Invalid file path, filePath=", filePath);
			//throw new GatewayliteException(GatewayliteErrorCode.SYSTEM_ERROR, "Invalid file path");
		}
		return url;
	}

	private List<String> readToStringListFromClassPath(String filePath) {
		InputStream inputFileStream = null;
		List<String> lines = new ArrayList<String>();
		try {
			inputFileStream = this.getClass().getClassLoader().getResourceAsStream(CLASSPATH_PREFIX.concat(filePath));
			AssertUtil.isNotNull(inputFileStream, GatewayErrorCode.SYSTEM_ERROR,
					String.format("can not find file, path: %s", filePath));
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputFileStream));
			bufferedReader.lines().forEach(line -> {
				lines.add(line);
			});
			return lines;
		} catch (Exception e) {
			LogUtil.error(logger, e, e.getMessage());
			throw new CommonException(GatewayErrorCode.CONFIGURATION_NOT_FOUND, e);
		} finally {
			if (null != inputFileStream) {
				try {
					inputFileStream.close();
				} catch (IOException e) {
					LogUtil.error(logger, e, e.getMessage());
				}
			}
		}
	}

	/**
	 *
	 */
	private String getFileName(String filePath) {
		int index = StringUtils.lastIndexOf(filePath, File.separatorChar);
		if (index == -1) {
			index = StringUtils.lastIndexOf(filePath, Constant.PATH_SEPARATOR);
		}
		return StringUtils.substring(filePath, index + 1);
	}

	/**
	 * get jar file from path
	 */
	private String getJarFile(String path) {
		String pathName;
		int jarIndex = StringUtils.indexOf(path, CLASS_JAR_FILE_SUFFIX);
		pathName = StringUtils.substring(path, 0, jarIndex) + JAR_FILE_SUFFIX;
		return StringUtils.substring(pathName, 5);
	}

	private String getClassJarFile(String path) {
		int jarIndex = StringUtils.indexOf(path, CLASS_JAR_FILE_SUFFIX);
		return StringUtils.substring(path, 0, jarIndex) + CLASS_JAR_FILE_SUFFIX + File.separator;
	}

	private List<String> getFilenameList(String filePath, String directory) {
		List<String> parserFileName = new ArrayList<String>();
		File file = new File(filePath);
		try (JarFile jarFile = new JarFile(file)) {

			for (Enumeration<JarEntry> e = jarFile.entries(); e.hasMoreElements(); ) {
				JarEntry jarEntry = e.nextElement();
				String fileName = jarEntry.getName();
				if (fileName.endsWith(".class") || fileName.endsWith(File.separator)) {
					continue;
				}

				if (fileName.contains(directory)) { //ASSEMBLE_DIRECTORY
					parserFileName.add(fileName);
				}
			}

		} catch (Exception e) {
			LogUtil.error(logger, e, "getFilenameList fail");
		}
		return parserFileName;
	}

}
