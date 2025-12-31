package mypkg;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

import com.google.common.collect.Lists;

public class AutoCompare {

	private static final String SERVER_BASE_PATH = "C:\\temp_t\\test222\\Devtools"; //"\\\\hoge_src\\foo\\資産\\src";

	private static final String LOCAL_BASE_PATH = "C:\\workspace\\Devtools";

	// ***********************************
	// コンソールと同じ内容を出力するログの出力先
	// ***********************************
	private static final boolean IS_NEED_LOG = true;
	private static final String LOG_DIR_PATH = "C:\\temp_t\\AutoCompare";
	// ***********************************

	// ***********************************
	// ファイルが無い時にスルーするモードかどうか
	// ***********************************
	private static final boolean IS_THROUGH_NOT_EXIST_FILE = false;
	private static final List<String> listlocalNotExistThrough = Lists.newArrayList();
	private static final List<String> listserverNotExistThrough = Lists.newArrayList();
	// ***********************************

	// 「CRLF」と「LF」の違いを無視した比較もするかどうか
	private static final boolean IS_SAME_WITH_CHANGE_CRLF_TO_LF = false;

	/** skip対象、削除しようとしてローカルから取り除いててまだ、開発資産やgitに反映していないときに怒られるなどを回避するため一時的にスキップ動作させたいファイルのリスト */
	private static final List<String> TEMP_SKIP_LIST = Lists.newArrayList();

	private static final byte byteCR = '\r';
	private static final byte byteLF = '\n';

	static {
		// 諸事情があり一時的に、比較をSKIPしたいものをここに追加する。
//		TEMP_SKIP_LIST.add("hogehoge.java");
//		TEMP_SKIP_LIST.add("foo.java");
	}

	private static String getNowTimesatampString() {
        Date d = new Date();
        String dt = getTimesatampString(d);
        return dt;
	}
	private static String getTimesatampString(Date d) {
        SimpleDateFormat d1 = new SimpleDateFormat("yyyyMMddHHmmss");
        String dt = d1.format(d);
        return dt;
	}

	private static class BothOut {
		private BufferedWriter bw = null;
		public BothOut() {
		}
		public BothOut(BufferedWriter bw) {
			this.bw = bw;
		}
		public void println(String x) throws Exception {
			if(this.bw != null) {
				this.bw.append(x).append("\r\n");
			}
			System.out.println(x);
		}

		public void printStackTrace(Throwable e) throws Exception {
			e.printStackTrace();

			this.println("*********************************************************************************************************");
			this.println("*********************************************************************************************************");
			this.println("*********************************************************************************************************");
			this.println("例外発生");
			this.println("*********************************************************************************************************");
			this.println("例外クラス[" + e.getClass().toString() + "] 例外メッセージ[" + e.getMessage() + "]");
			this.println("*********************************************************************************************************");
			this.println("スタックトレース");
			this.println("*********************************************************************************************************");
			this.println(e.getMessage());
			StackTraceElement[] stacks = e.getStackTrace();
			for (StackTraceElement element : stacks) {
				String line = "\t" + "at " + String.valueOf(element);
				this.println(line);
			}
			this.println("*********************************************************************************************************");
			this.println("*********************************************************************************************************");
			this.println("*********************************************************************************************************");
		}
	}

	public static void main(String[] args) throws Exception {

		if(IS_NEED_LOG) {
			String nowTimesatampString = getNowTimesatampString();

			File file = new File(LOG_DIR_PATH + "\\" + "log" + nowTimesatampString + ".log");
			try (
				BufferedWriter bw = new BufferedWriter(new FileWriter(file));
			) {
				BothOut out = new BothOut(bw);
				try {
					mainProc(out);
				} catch(Exception e) {
					out.printStackTrace(e);
					throw e;
				}
			}
		} else {
			BothOut out = new BothOut();
			mainProc(out);
		}

	}

	public static void mainProc(BothOut out) throws Exception {

		RecursiveFileListGetter r0 = new RecursiveFileListGetter();
		r0.setBasePath(SERVER_BASE_PATH);

		// r0.setPatternString("([^\\s]+(\\.(?i)(css|html|js|java|xml))$)");
		r0.collectList();

		List<String> fileServerList = r0.getFilePathList();

		List<String> localList = Lists.newArrayList();
		for (int index = 0 ; index < fileServerList.size() ; ++index) {

			String path = fileServerList.get(index);

			String localPath = LOCAL_BASE_PATH + path.substring(SERVER_BASE_PATH.length());

			localList.add(localPath);

		}

		if(localList.size() != fileServerList.size()) {
			throw new Exception("件数不一致");
		}

		out.println("################################# START");
		for (int index = 0 ; index < localList.size() ; ++index) {
			String localPath = localList.get(index);
			File localFile = new File(localPath);

			String serverPath = fileServerList.get(index);
			File serverFile = new File(serverPath);

			if(serverPath.endsWith("Thumbs.db")) {
				continue;
			}

			boolean isTempSkip = isTempSkip(serverPath);
			if(isTempSkip) {
				continue;
			}

			if(!localFile.exists()) {
				String msg = "local[" + localFile + "] server[" + serverFile + "]  local file not found";
				if(IS_THROUGH_NOT_EXIST_FILE) {
					listlocalNotExistThrough.add(msg);
					continue;
				} else {
					throw new Exception(msg);
				}
			}

			if(!serverFile.exists()) {
				String msg = "local[" + localFile + "] server[" + serverFile + "]  server file not found";
				if(IS_THROUGH_NOT_EXIST_FILE) {
					listserverNotExistThrough.add(msg);
					continue;
				} else {
					throw new Exception(msg);
				}
			}

			if(IS_SAME_WITH_CHANGE_CRLF_TO_LF) {
				// 「CRLF」と「LF」の違いを無視した比較もする場合

				boolean isSame = fileCompare3(localPath, serverPath);
				boolean isSameWithChangeCrLfToLf = fileCompare3WithChangeCrLfToLf(localPath, serverPath);

				String contains = isSame + "\t" + isSameWithChangeCrLfToLf + "\t" + localPath + "\t" + serverPath;
				out.println(contains);
			} else {
				boolean isSame = fileCompare3(localPath, serverPath);

				String contains = isSame + "\t" + localPath + "\t" + serverPath;
				out.println(contains);
			}
		}
		out.println("################################# END");

		if(IS_THROUGH_NOT_EXIST_FILE) {
			if(CollectionUtils.isNotEmpty(listlocalNotExistThrough)) {
				out.println("#################################");
				out.println("#################################");
				out.println("#################################");
				out.println("[listlocalNotExistThrough] ################################# START");
				for(String str : listlocalNotExistThrough) {
					out.println(str);
				}
				out.println("[listlocalNotExistThrough] ################################# END");
			}

			if(CollectionUtils.isNotEmpty(listserverNotExistThrough)) {
				out.println("#################################");
				out.println("#################################");
				out.println("#################################");
				out.println("[listserverNotExistThrough] ################################# START");
				for(String str : listserverNotExistThrough) {
					out.println(str);
				}
				out.println("[listserverNotExistThrough] ################################# END");
			}
		}
	}

	private static boolean isTempSkip(String serverPath) {
		if(CollectionUtils.isEmpty(TEMP_SKIP_LIST)) {
			return false;
		}

		boolean isTempSkip = false;
		for (String tempSkipName : TEMP_SKIP_LIST) {
			if(serverPath.endsWith(tempSkipName)) {
				isTempSkip = true;
				break;
			}
		}

		return isTempSkip;
	}

	public static boolean fileCompare3(String fileA, String fileB) throws Exception {
	    boolean bRet = false;

        if( new File(fileA).length() != new File(fileA).length() ){
            return bRet;
        }
        byte[] byteA = Files.readAllBytes(Paths.get(fileA));
        byte[] byteB = Files.readAllBytes(Paths.get(fileB));
        bRet = Arrays.equals(byteA, byteB);
//        if(!bRet){
//            System.out.println(new String(byteA,"UTF-8"));
//            System.out.println(new String(byteB,"UTF-8"));
//        }
	    return bRet;
	}

	public static boolean fileCompare3WithChangeCrLfToLf(String fileA, String fileB) throws Exception {
	    boolean bRet = false;

        if( new File(fileA).length() != new File(fileA).length() ){
            return bRet;
        }
        byte[] byteA = Files.readAllBytes(Paths.get(fileA));
        byteA = changeCrLfToLf(byteA);

        byte[] byteB = Files.readAllBytes(Paths.get(fileB));
        byteB = changeCrLfToLf(byteB);

        bRet = Arrays.equals(byteA, byteB);
//        if(!bRet){
//            System.out.println(new String(byteA,"UTF-8"));
//            System.out.println(new String(byteB,"UTF-8"));
//        }
	    return bRet;
	}

	/**
	 * CRLFをLFに変換する。
	 * @param srcBytes
	 * @return
	 */
	private static byte[] changeCrLfToLf(byte[] srcBytes) {
		if(srcBytes == null) {
			return null;
		}
		if(srcBytes.length == 0) {
			return new byte[0];
		}

		int length = srcBytes.length;
		byte[] tempArray = new byte[length];
		int tempLength = 0;
		for (int index = 0 ; index < length ; ++index) {

			byte currentByte = srcBytes[index];

			int nextIndex = ( index + 1 );
			boolean isInner = (nextIndex < length);
			if(!isInner) {
				tempArray[tempLength] = currentByte;
				++tempLength;
				continue;
			}

			byte nextByte = srcBytes[nextIndex];

			if( (currentByte == byteCR) && (nextByte == byteLF) ) {
				tempArray[tempLength] = byteLF;
				++tempLength;

				++index;

				continue;
			}

			tempArray[tempLength] = currentByte;
			++tempLength;
		}

		byte[] retArray = new byte[tempLength];
		for (int index = 0 ; index < tempLength ; ++index) {
			retArray[index] = tempArray[index];
		}

		return retArray;
	}
}
