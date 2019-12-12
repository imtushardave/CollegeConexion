package com.cloudiera.collegeconexion.Utils;

import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by HP on 20-Dec-17.
 */

public class CheckInputs {

    private static final String TAG = "CheckInputs";

    private static final int MAX_LENGTH = 15 ;

    /**
     * method is used for checking valid email id format.
     * @param email
     * @return boolean true for valid false for invalid
     */
    public static boolean isEmailValid(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    /**
     * method is used for checking valid roll no format.
     * @param rollno
     * @return boolean true for valid ,false for invalid
     */
    public static boolean isRollNoValid(String rollno){
        String expression = "^[\\d{2}+]+/(\\d{3}+)$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(rollno);
        return matcher.matches();
    }

    /**
     * method is used to checking valid phone number
     * @param phone
     * @return boolean true for valid, false for invalid
     */
    public static boolean isValidMobile(String phone) {
        return android.util.Patterns.PHONE.matcher(phone).matches();
    }
    /*
    Generate Random String for the user
     */
    public static String random() {
        String randomString;
      do{
          Random generator = new Random();
          StringBuilder randomStringBuilder = new StringBuilder();
          int randomLength = generator.nextInt(MAX_LENGTH);
          char tempChar;
          for (int i = 0; i < randomLength; i++){
              tempChar = (char) (generator.nextInt(96) + 30);
              randomStringBuilder.append(tempChar);
          }
          randomString = randomStringBuilder.toString();

      }while(randomString.equals(""));

        return randomString;
    }



    public static String getCourseYear(String rollNo){

        Log.d(TAG, "getYear: Calculating the Year of the Student" + rollNo);
        if(rollNo != null){
            rollNo = rollNo.substring(0,2);
            String admissionYear = "20"+ rollNo ;
            String year = null  ;
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd",Locale.US);
            Date dateCurrent = new Date();
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy/MM/dd", Locale.US);
            admissionYear.trim();
            String admissionDate = admissionYear + "/08/01";
            Date dateAdmission = null;
            try {
                dateAdmission = fmt.parse(admissionDate);
                dateCurrent = dateFormat.parse("yyyy/MM/dd");
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Log.d(TAG, "getYear: dateCurrent :: "+ dateCurrent);
            Log.d(TAG, "getYear: datePassout :: "+ dateAdmission);
            //in milliseconds
            if(dateAdmission != null){

                long diff =  dateCurrent.getTime() -  dateAdmission.getTime();
                long diffDays = diff / (24 * 60 * 60 * 1000);
                Log.d(TAG, "getYear: diffDays :: "+ diffDays);
                if(diffDays > 4*365){
                    year = "Alumni";
                }else if (diffDays >= 3*365){
                    year = "Final Year";
                }else if(diffDays>=2*365){
                    year = "IIIrd Year";
                }else if(diffDays >= 365){
                    year = "IInd Year";
                }else {
                    year = "Ist Year";
                }
                return year;
            }

        }
        return " ";
    }


    public static String capitalString(String input){

             input = input.toLowerCase();
            String result = "";
            char firstChar = input.charAt(0);
            result = result + Character.toUpperCase(firstChar);
            for (int i = 1; i < input.length(); i++) {
                char currentChar = input.charAt(i);
                char previousChar = input.charAt(i - 1);
                if (previousChar == ' ') {
                    result = result + Character.toUpperCase(currentChar);
                } else {
                    result = result + currentChar;
                }
            }
            System.out.println(result);
            return result;

    }
}
