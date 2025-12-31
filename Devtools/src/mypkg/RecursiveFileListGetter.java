package mypkg;

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Lists;

public class RecursiveFileListGetter {

	private boolean isContains = false;
	private String patternString = null;

	private String basePath = null;
	private List<String> fileNameList = Lists.newArrayList();
	private List<String> filePathList = Lists.newArrayList();

	public boolean isContains() {
		return isContains;
	}
	public void setContains(boolean isContains) {
		this.isContains = isContains;
	}

	public void setBasePath(String basePath) {
		this.basePath = basePath;
	}

	public void setPatternString(String patternString) {
		this.patternString = patternString;
	}

	public List<String> getFileNameList() {
		return fileNameList;
	}

	public List<String> getFilePathList() {
		return filePathList;
	}

	public void collectList() {
		fileNameList.clear();
		filePathList.clear();
		if(StringUtils.isBlank(basePath)) {
			return;
		}

		File baseDir = new File(basePath);
		if(!baseDir.isDirectory()) {
			return;
		}

		collectListRecursive(baseDir);
	}
	private void collectListRecursive(File baseDir) {

		File[] fileList = baseDir.listFiles();
		if(fileList != null){
			for(File f: fileList){
				if(f.isDirectory()) {
					collectListRecursive(f);
				} else {
					boolean isOKFile = isOKFile(f);
					if(isOKFile) {
						fileNameList.add(f.getName());
						filePathList.add(f.getAbsolutePath());
					}

				}
			}
		}
	}

	private boolean isOKFile(File file) {
		if(StringUtils.isBlank(patternString)) {
			return true;
		}

		if(isContains) {
			boolean ret = false;
			if(StringUtils.isNotBlank(file.getName())) {
				ret = file.getName().contains(patternString);
			}
			return ret;
		}

		Pattern pattern = Pattern.compile(patternString);
		Matcher matcher = pattern.matcher(file.getName());
		return matcher.matches();
	}
}
