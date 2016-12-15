package org.tempuri;

import javax.jws.WebMethod;

/**
 * Created by Administrator on 2016/10/25.
 */
public interface WebService {

    @WebMethod
    public String CallService(String CLIENTNO, String TaxMachineIP, String SysInvNo, String InvoiceList, String InvoiceSplit, String InvoiceConsolidate, String InvoiceData) throws Exception;

    @WebMethod
    public String KPCallService(String CLIENTNO, String TaxMachineIP, String SysInvNo, String InvoiceList, String InvoiceSplit, String InvoiceConsolidate, String InvoiceData);

    @WebMethod
    public String RePrint(String CLIENTNO, String TaxMachineIP, String InvoiceData);

    @WebMethod
    public String GetCodeAndNo(String CLIENTNO, String TaxMachineIP, String fplxdm);

    @WebMethod
    public String GetFPStock(String CLIENTNO, String TaxMachineIP, String fplxdm);

    @WebMethod
    public String GetFp(String CLIENTNO, String TaxMachineIP, String fplxdm, String cxfs, String cxtj, String cxlx);

    @WebMethod
    public String CheckSysInvNo(String _SysInvNo);

    @WebMethod
    public String GetClientTaxCardInfo(String _CLIENTNO);

    @WebMethod
    public String GetTaxCardInfo(String _TAXCARDNO);

    @WebMethod
    public String GetTaxCardAndSfdm(String _TAXCARDNO, String _NSRSBH);

    @WebMethod
    public boolean SetIP(String TAXCARDNO, String CLIENTIP, String Kpjh);

    @WebMethod
    public boolean UpdateT_XF_KPXE(String xfsh, String zpmaxje, String ppmaxje);

    @WebMethod
    public boolean SetKL(String TAXCARDNO, String skpkl);

    @WebMethod
    public boolean SetKeyPwd(String TAXCARDNO, String KeyPwd);

    @WebMethod
    public boolean SetAutoPrint(String TAXCARDNO, String AutoPrint);

    @WebMethod
    public String GetData(String sysInvNo, String kpStart, String kpEnd, String zfStart, String zfEnd, String operateFlag, String printFlag);

    @WebMethod
    public boolean CheckWebService();

    @WebMethod
    public String CheckDHCPIP();

    @WebMethod
    public String Test(String x);

    @WebMethod
    public boolean InsertErrorLog(String TaxMachineIP, String TaxCardNo, String OperateType, String ErrorMsg, String CR_XML, String CC_XML);

    @WebMethod
    public void WriteInputInfoToDB(String taxCardNO, String TaxMachineIP, String CLIENTNO, String SysInvNo, String InvoiceList, String InvoiceSplit, String InvoiceConsolidate, String InvoiceData);

    @WebMethod
    public String CheckSPBM(String _SPBM);

    @WebMethod
    public boolean InsertSPDZ(String SPMC, String SPBM, String BBH, String MHBZ);

    @WebMethod
    public String CheckData(String spbm, String spmc);

    @WebMethod
    public String CheckBBH();

    @WebMethod
    public boolean isDeleteSPDZ(String SPMC, String SPBM, String BBH, String MHBZ);

    @WebMethod
    public int SelectSPMC(String SPMC, String BBH);

    @WebMethod
    public int SelectID(int ID);

    @WebMethod
    public String SelectFromID(int ID);

    @WebMethod
    public String SelectDefSPBM();

    @WebMethod
    public boolean UpdateSPDZ(int ID, String SPMC, String SPBM, String BBH, String MHBZ);

    @WebMethod
    public boolean UpdateParameterSpbm(String SPBM, String YXBZ);

    @WebMethod
    public int spdzdatatable(String SPMC, String SPBM, String BBH, String MHBZ);

    @WebMethod
    public String SelectReadTime();

    @WebMethod
    public boolean UpdateReadTime(String readtime);

    @WebMethod
    public String InvoiceUpload(String CLIENTNO, String TaxMachineIP, String fplxdm);

    @WebMethod
    public String InvoiceWrieBack(String CLIENTNO, String TaxMachineIP, String fplxdm);

    @WebMethod
    public String InvoiceUnUploadCount(String CLIENTNO, String TaxMachineIP, String fplxdm);

    @WebMethod
    public String FpUpload(String CLIENTNO, String TaxMachineIP, String fplxdm);

    @WebMethod
    public String InvFromSysInvNo(String _SysInvNo);

    @WebMethod
    public String getParaMeter(String _ParaKey);

    @WebMethod
    public boolean WriteFpxx(String ClientNo, String Fplxdm, String Pch, String strFpxxOutput) throws Exception;
}
