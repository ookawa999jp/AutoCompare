package mypkg;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import com.google.common.collect.Lists;

public class ManualCompare {

	private static class Item {
		public List<String> fileServerList = null;
		public List<String> localList = null;
	}

	// ABC対応
	private static Item abcTaiou(boolean isTaiouBun) {

		String srvBase = null;
		if(isTaiouBun) {
			srvBase = "\\\\hoge_srv\\abc\\成果物\\";
		} else {
			srvBase = "\\\\hoge_srv\\abc\\対応前\\";
		}

		List<String> fileServerList = Lists.newArrayList();

		// ファイルサーバー成果物のファイルを指定
		fileServerList.add(srvBase + "CCC.java");
		fileServerList.add(srvBase + "DDD.html");
		fileServerList.add(srvBase + "EEEController.java");

		List<String> localList = Lists.newArrayList();

		// ローカルのproject内ファイルを指定
		localList.add("C:\\Myproject\\CCC.java");
		localList.add("C:\\Myproject\\templates\\ddd\\DDD.html");
		localList.add("C:\\Myproject\\EEEController.java");

		Item item = new Item();
		item.fileServerList = fileServerList;
		item.localList = localList;
		return item;
	}

	// ABC対応_MotoCompare
	private static Item abcTaiouMotoCompare() {

		String srvBase = "\\\\hoge_srv\\abc\\対応前\\";

		List<String> fileServerList = Lists.newArrayList();

		// ファイルサーバー対応前のファイルを指定
		fileServerList.add(srvBase + "CCC.java");
		fileServerList.add(srvBase + "DDD.html");
		fileServerList.add(srvBase + "EEEController.java");

		List<String> localList = Lists.newArrayList();

		// gitなどで最新をcheckoutしたフォルダなどを指定 ( ファイルサーバー対応前と比較し、他の人の変更を検知するのが目的 )
		localList.add("C:\\checkoutDir\\project\\CCC.java");
		localList.add("C:\\checkoutDir\\project\\templates\\ddd\\DDD.html");
		localList.add("C:\\checkoutDir\\project\\EEEController.java");

		Item item = new Item();
		item.fileServerList = fileServerList;
		item.localList = localList;
		return item;
	}


	public static void main(String[] args) throws Exception {

		Item item = null;

		// ABC対応
		if(true) {

			System.out.println("##############################################################");
			System.out.println("ABC対応");
			System.out.println("##############################################################");

			for(int index = 0 ; index < 3 ; ++index) {
				if(index == 0) {
					final boolean isTaiouBun = true; // 対応分のとき
					System.out.println("##############");
					System.out.println("対応分とローカル");
					System.out.println("##############");


					System.out.println("スキップ");
					// ファイルサーバーとローカルの比較
					item = abcTaiou(isTaiouBun);

					doLogic(item);
				}

				if(index == 1) {
					final boolean isTaiouBun = false; //_調査前のとき
					System.out.println("##############");
					System.out.println("調査前とローカル");
					System.out.println("##############");

					// ファイルサーバーとローカルの比較
					item = abcTaiou(isTaiouBun);

					doLogic(item);
				}

				if(index == 2) {
					System.out.println("##############");
					System.out.println("調査前とベース");
					System.out.println("##############");
					// _調査前 と ベースとの
					// ファイルサーバーとローカルの比較( ベースとのコンペア
					item = abcTaiouMotoCompare();

					doLogic(item);
				}
			}

			System.out.println("##############################################################");
			System.out.println("");
			System.out.println("");
			System.out.println("");
		}

	}

	private static void doLogic(Item item) throws Exception {
		List<String> fileServerList = item.fileServerList;
		List<String> localList = item.localList;

		if(localList.size() != fileServerList.size()) {

			if(true) {

				System.out.println("**********************");
				System.out.println("localList ************");
				System.out.println("**********************");
				for(String s : localList) {
					String[] tempArray = s.split("\\\\");
					System.out.println(tempArray[tempArray.length - 1]);
				}
				System.out.println("**********************");

				System.out.println("**********************");
				System.out.println("fileServerList ********");
				System.out.println("**********************");
				for(String s : fileServerList) {
					String[] tempArray = s.split("\\\\");
					System.out.println(tempArray[tempArray.length - 1]);
				}
				System.out.println("**********************");
			}


			throw new Exception("件数不一致 localList.size()[" + localList.size() + "] fileServerList.size()[" + fileServerList.size() + "]");
		}

		System.out.println("################################# START");
		for (int index = 0 ; index < localList.size() ; ++index) {
			String localPath = localList.get(index);
			File localFile = new File(localPath);
			if(!localFile.exists()) {
				throw new Exception("[" + localFile + "] is not found");
			}

			String serverPath = fileServerList.get(index);

			File serverFile = new File(serverPath);
			if(!serverFile.exists()) {
				throw new Exception("[" + serverFile + "] is not found");
			}

			boolean isSame = fileCompare3(localPath, serverPath);

			String contains = isSame + "\t" + localPath + "\t" + serverPath;
			System.out.println(contains);
//			if(isSame) {
//				System.out.println(contains);
//			} else {
//				String winmergeCommandLine = "'\"C:\\Program Files\\WinMerge\\WinMergeU.exe\" -wl -wr \"" + localPath + "\" \"" + serverPath + "\"";
//				System.out.println(contains + "\t" + winmergeCommandLine);
//			}
		}
		System.out.println("################################# END");
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
}
