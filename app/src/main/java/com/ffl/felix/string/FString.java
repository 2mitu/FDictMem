package com.ffl.felix.string;

/**
 * Created by PengfeiLin on 2017/12/20.
 */
import java.util.regex.*;
import java.util.Arrays;

public class FString {
    public static String stringOfSameChar(char ch, int size) {
        //生成size个相同字符ch的字符串
        final char[] array = new char[size];
        Arrays.fill(array, ch);
        return new String(array);
    }

    public static String hideChineseCharacters(char showChar, String oriStr) {
        // 将字符串中的中文字符替换为showChar
        String pStr = "([\u4e00-\u9fa5]+)";
        Pattern ptn = Pattern.compile(pStr);
        Matcher mtch = ptn.matcher(oriStr);
        String rStr;
        StringBuffer newStrBuf = new StringBuffer();
        while(mtch.find())
        {
            rStr = stringOfSameChar(showChar, mtch.end() - mtch.start());
            mtch.appendReplacement(newStrBuf, rStr);
        }
        mtch.appendTail(newStrBuf);
        return newStrBuf.toString();
    }
}
