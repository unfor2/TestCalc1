package com.unfor2.testcalc1;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {
    TextView tvDisplay;
    String stDisplayText;
    boolean bDotFlag;
    SharedPreferences sPref;

    final String SAVED_TEXT = "saved_display";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvDisplay = (TextView) findViewById(R.id.tvDisplay);


        onClick_btnC(tvDisplay);
    }


    protected  void  onClick_btnC(View v) {
        //
        stDisplayText = "0";
        bDotFlag = false;


        //tvDisplay.setText(stDisplayText);

        loadDisplay();


        doDisplayText();
    }


    protected  void  onClick_Digit(View v) {
        //
        String txt;
        Button btn;
        btn = (Button) v;

        txt = btn.getText().toString();

        stDisplayText = ((!stDisplayText.equals("0"))? stDisplayText:"") + txt;



        doDisplayText();
    }

    protected void onClickDot(View v) {
       if (!bDotFlag) {
           stDisplayText = stDisplayText + '.';
       }

        bDotFlag = true;
    }

    protected void btnDelete(View v){
        if (!(stDisplayText.equals("") || stDisplayText.equals(""))) {
           stDisplayText = stDisplayText.substring(0, stDisplayText.length() - 1);



        }

        doDisplayText();

    }


    protected void loadDisplay () {
        sPref = getPreferences(MODE_PRIVATE);
        stDisplayText = sPref.getString(SAVED_TEXT, "");
    }

    protected  void saveDisplay () {
        sPref = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString(SAVED_TEXT, stDisplayText);
        ed.commit();
    }

    public static double eval(final String str) {
        return new Object() {
            int pos = -1, ch;

            void nextChar() {
                ch = (++pos < str.length()) ? str.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            double parse() {
                nextChar();
                double x = parseExpression();
                if (pos < str.length()) throw new RuntimeException("Unexpected: " + (char)ch);
                return x;
            }

            // Grammar:
            // expression = term | expression `+` term | expression `-` term
            // term = factor | term `*` factor | term `/` factor
            // factor = `+` factor | `-` factor | `(` expression `)`
            //        | number | functionName factor | factor `^` factor

            double parseExpression() {
                double x = parseTerm();
                for (;;) {
                    if      (eat('+')) x += parseTerm(); // addition
                    else if (eat('-')) x -= parseTerm(); // subtraction
                    else return x;
                }
            }

            double parseTerm() {
                double x = parseFactor();
                for (;;) {
                    if      (eat('*')) x *= parseFactor(); // multiplication
                    else if (eat('/')) x /= parseFactor(); // division
                    else return x;
                }
            }

            double parseFactor() {
                if (eat('+')) return parseFactor(); // unary plus
                if (eat('-')) return -parseFactor(); // unary minus

                double x;
                int startPos = this.pos;
                if (eat('(')) { // parentheses
                    x = parseExpression();
                    eat(')');
                } else if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = Double.parseDouble(str.substring(startPos, this.pos));
                } else if (ch >= 'a' && ch <= 'z') { // functions
                    while (ch >= 'a' && ch <= 'z') nextChar();
                    String func = str.substring(startPos, this.pos);
                    x = parseFactor();
                    if (func.equals("sqrt")) x = Math.sqrt(x);
                    else if (func.equals("sin")) x = Math.sin(Math.toRadians(x));
                    else if (func.equals("cos")) x = Math.cos(Math.toRadians(x));
                    else if (func.equals("tan")) x = Math.tan(Math.toRadians(x));
                    else throw new RuntimeException("Unknown function: " + func);
                } else {
                    throw new RuntimeException("Unexpected: " + (char)ch);
                }

                if (eat('^')) x = Math.pow(x, parseFactor()); // exponentiation

                return x;
            }
        }.parse();
    }

    protected void doDisplayText() {

        Double ev;

        //ev = 0.0;
        String stEv;


        try {
            ev = eval(stDisplayText);

            saveDisplay();
            stEv = " = " + ev.toString();


        } catch (RuntimeException e){

            stEv = "";
        };



        tvDisplay.setText(stDisplayText + stEv);
    }
}
