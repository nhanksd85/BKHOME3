package com.example.bkhome;

public class AssistantModel {

    private String strName;
    private String strJobDescription;
    private String strEmail;
    private String strPhoneCall;
    private int mDrawableLogo = 0;

    public AssistantModel(String name, String job, String email, String phonecall, int icon){
        strName = name;
        strJobDescription = job;
        strEmail = email;
        strPhoneCall = phonecall;
        setmDrawableLogo(icon);
    }



    public String getStrName() {
        return strName;
    }

    public void setStrName(String strName) {
        this.strName = strName;
    }

    public String getStrJobDescription() {
        return strJobDescription;
    }

    public void setStrJobDescription(String strJobDescription) {
        this.strJobDescription = strJobDescription;
    }

    public String getStrEmail() {
        return strEmail;
    }

    public void setStrEmail(String strEmail) {
        this.strEmail = strEmail;
    }

    public String getStrPhoneCall() {
        return strPhoneCall;
    }

    public void setStrPhoneCall(String strPhoneCall) {
        this.strPhoneCall = strPhoneCall;
    }

    public int getmDrawableLogo() {
        return mDrawableLogo;
    }

    public void setmDrawableLogo(int mDrawableLogo) {
        this.mDrawableLogo = mDrawableLogo;
    }

}
