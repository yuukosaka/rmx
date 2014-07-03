package logic.propfile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import dao.PropfileDao;

/**
 * env.propertiesのチェックを行う.<br/>
 * キー(domain)から値を取り出す.(domain = ドメイン名:ホスト名)<br/>
 * 次にキー(ドメイン名)から値を取り出し、propertiesファイルを決定する.<br/>
 * (ex)env.propertiesファイルのキー(domain)の値がrmxdev:rmxdev.keio.jpのとき、キーとして
 * rmxdevが必要となる. <br/>
 * その値testkを取得したとき、用意するpropertiesファイルはtestk_rmxdevである.
 */
public final class PropFileService {
	// メンバ変数
	private final String ENVPROPFILENAME = "env";
	private HashMap<String, String> hashOfFileAndDomain;
	private ArrayList<HashMap<String, String>> setOfDomconfObj;
	private String recipient;
	private ResourceBundle envBundle;
	private HashMap<String, ResourceBundle> domBundles;
	private static PropFileService pfs = new PropFileService();

	// コンストラクタ
	private PropFileService() {
		hashOfFileAndDomain = new HashMap<String, String>();
		setOfDomconfObj = new ArrayList<HashMap<String, String>>();
		envBundle = PropfileDao.readPropFile(ENVPROPFILENAME);
		domBundles = new HashMap<String, ResourceBundle>();
	}

	public static PropFileService getInstance() {
		return pfs;
	}

	/**
	 * env.propertiesを読み込み、domconfのファイル名をキーとしてそのpropertiesを提供するマップを格納
	 */
	public void init() {
		// 1. env.propertiesのdomainから値を取得
		String domainNames = envBundle.getString("domain");
		// 2. domainNamesを","で分割
		String[] domainName = domainNames.split(",");

		// 3. domainNameを":"で分割し、domconfのバンドル名を取得
		for(int i = 0; i < domainName.length; i++){
			String[] propertiesAndAddress = domainName[i].split(":");
			String formerDomainName = propertiesAndAddress[0];
			String latterDomainName = propertiesAndAddress[1];
			String properties = envBundle.getString(formerDomainName);
			String[] property = properties.split(",");
			for (int j = 0; j < property.length; j++){
				if (this.isExist(formerDomainName + "_" + property[j]))
					domBundles.put(property[j] + "." + latterDomainName, ResourceBundle.getBundle(formerDomainName + "_" + property[j]));
			}
		}
		// 4. 最後に、default.propertiesを追加
		domBundles.put("default", ResourceBundle.getBundle("default"));
		return;
	}

	/** env.propertiesファイルを読み込み、fulldomain,domain,.propertiesファイルをHashMapに格納 */
	public ArrayList<HashMap<String, String>> getDomconfPropFileNamesAndDomains() {

		/*
		 * env.propertiesからキーdomainから値を取得
		 * (ex)domainNames->rmxdev:rmxdev.db.ics.keio
		 * .ac.jp,rmx-keio:rmx-keio.netなど
		 */
		String domainNames = envBundle.getString("domain");
		/*
		 * domainNamesを","で分割する
		 * (ex)domainName[0]->rmxdev:rmxdev.db.ics.keio.ac.jp
		 * ,domainName[1]->rmx-keio:rmx-keio.net
		 */
		String[] domainName = domainNames.split(",");

		// firstDomainName->(ex)rmxdev
		String[] firstDomainName = new String[domainName.length];
		// lastDomainName->(ex)rmxdev.db.ics.keio.ac.jp
		String[] lastDomainName = new String[domainName.length];

		for (int i = 0; i < domainName.length; i++) {
			/*
			 * domainName[i]を":"で分割する
			 * (ex)firstDomainName[0]->rmxdev,firstDomainName[1]->rmx-keio
			 * lastDomainName
			 * [0]->rmxdev.db.ics.keio.ac.jp,lastDomainName[1]->rmx-keio.net
			 */
			String[] tmp = domainName[i].split(":");
			firstDomainName[i] = tmp[0];
			lastDomainName[i] = tmp[1];

			// default.propertiesを格納
			hashOfFileAndDomain.put("propFileName", "default");
			hashOfFileAndDomain.put("fullDomainName", lastDomainName[i]);
			hashOfFileAndDomain.put("domainName", lastDomainName[i]);
			setOfDomconfObj.add(hashOfFileAndDomain);

			// env.propertiesファイル内にfirstDomain[i]のキーが存在すれば
			if (envBundle.containsKey(firstDomainName[i])) {
				// (ex)valuesOfFirstDomainKey->testk,testb
				String valuesOfFirstDomainKey = envBundle
						.getString(firstDomainName[i]);
				// (ex)personalDomaninName[0]->testk,personalDomaninName[1]->testb
				String[] personalDomainName = valuesOfFirstDomainKey.split(",");

				for (int j = 0; j < personalDomainName.length; j++) {
					/*
					 * (ex)"propFileName"->testk_rmxdev,
					 * "fullDomainName"->testk.rmxdev.db.ics.keio.ac.jp
					 * "domainName"->rmxdev.db.ics.keio.ac.jp
					 */
					hashOfFileAndDomain = new HashMap<String, String>();
					//
					hashOfFileAndDomain.put("propFileName", firstDomainName[i]
							+ "_" + personalDomainName[j]);
					hashOfFileAndDomain.put("fullDomainName",
							personalDomainName[j] + "." + lastDomainName[i]);
					hashOfFileAndDomain.put("domainName", lastDomainName[i]);

					//
					setOfDomconfObj.add(hashOfFileAndDomain);
				}
			}
		}
		return setOfDomconfObj;
	}
	
	public ResourceBundle searchDomBundle(String recipient){
		String fullDomain = recipient.substring(recipient.indexOf("@") + "@".length());
		return domBundles.get(fullDomain);
	}
	
	private boolean isExist(String key){
		try {
			ResourceBundle.getBundle(key);
		} catch (MissingResourceException e){
			return false;
		}
		return true;
	}

	// メールアドレスを引数として、env.propertiesファイルから適切な.propertiesファイル名を返す
	// ex) testk_rmxdevなど
	public String getPropFileName(String _recipient) {
		recipient = _recipient;
		String propFileName = new String();

		// 宛先の@までを切り取る ex)test@test.com -> test.com
		int start = recipient.indexOf("@");
		String fullDomain = recipient.substring(start + 1);

		// env.propertiesファイルから適切なドメインかチェックして、正しければファイル名を返す
		ArrayList<HashMap<String, String>> hmp = this
				.getDomconfPropFileNamesAndDomains();
		for (int i = 0; i < hmp.size(); i++) {
			if (hmp.get(i).get("fullDomainName").equalsIgnoreCase(fullDomain)) {
				propFileName = hmp.get(i).get("propFileName");
				break;
			} else
				propFileName = null;
		}
		return propFileName;
	}

	public ResourceBundle getEnvBundle() {
		return envBundle;
	}

	public HashMap<String, ResourceBundle> getDomBundles() {
		return domBundles;
	}
}
