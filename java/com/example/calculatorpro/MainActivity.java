package com.example.calculatorpro;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.content.Intent;

/*
    editText: Input box
    text: Error message box
    str: Expression for computation
    indexYN: Error flag
    infixExpression: Infix expression
    suffixExpression: Postfix expression
 */
public class MainActivity extends AppCompatActivity {

    private EditText editText;
    private TextView text;
    private final StringBuilder str = new StringBuilder();
    private int indexYN = 0;

    List<String> calculationHistory = new ArrayList<>();

    private DatabaseHelper dbHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        dbHelper = new DatabaseHelper(this);
        editText = (EditText) findViewById(R.id.editView);
        text = (TextView) findViewById(R.id.historyTextView);

        TextView historyTextView = findViewById(R.id.historyTextView);
        historyTextView.setText("Calculation History:\n");

        for (String calculation : calculationHistory) {
            historyTextView.append(calculation + "\n");
        }


        // 获取显示历史记录的按钮
        Button historyButton = findViewById(R.id.historyButton);

        // 为按钮添加点击事件监听器
        historyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 在点击按钮时显示历史记录
                showCalculationHistory(historyTextView);
                //openHistoryActivity((ArrayList<String>) calculationHistory);
            }
        });
    }




    private void showCalculationHistory(TextView historyTextView) {
        loadCalculationHistoryFromDatabase(historyTextView);
        // 在这里加载历史记录，或者直接使用已加载的历史记录
        // 你可以添加逻辑来弹出对话框、新的活动或其他方式来显示历史记录
        // 这里只是简单地在 TextView 中显示历史记录
    }

    public void openHistoryActivity(ArrayList<String> cH) {
        // 创建一个 Intent，指定要打开的新的 Activity
        Intent intent = new Intent(this, HistoryActivity.class);

        // 获取历史数据，假设你有一个名为 historyList 的 List
        ArrayList<String> historyList = cH;

        // 将历史数据作为额外信息传递给新的 Activity
        intent.putStringArrayListExtra("historyData", historyList);

        // 启动新的 Activity
        startActivity(intent);
    }




    private void saveCalculationToDatabase(String calculation) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_CALCULATION, calculation);
        db.insert(DatabaseHelper.TABLE_HISTORY, null, values);
        db.close();
    }


    private void loadCalculationHistoryFromDatabase(TextView historyTextView) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] columns = {DatabaseHelper.COLUMN_CALCULATION};
        String orderBy = DatabaseHelper.COLUMN_ID + " DESC"; // 按最新的排序

        Cursor cursor = db.query(DatabaseHelper.TABLE_HISTORY, columns, null, null, null, null, orderBy);

        List<String> CH = new ArrayList<>();

        int count = 0;
        while (cursor.moveToNext() && count < 10) {
            @SuppressLint("Range") String calculation = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_CALCULATION));
            // 将历史记录添加到历史记录视图或适配器中
            CH.add(calculation);
            historyTextView.append(calculation + " | ");
            count++;
        }

        openHistoryActivity((ArrayList<String>) CH);

        cursor.close();
        db.close();
    }




    //1234567890+-.()
    public void clickButton(View view) {
        Button button = (Button) view;
        editText.append(button.getText());
        str.append(button.getText());
    }

    //divide
    public void div(View view) {
        editText.append("/");
        str.append("/");
    }

    //multiply
    public void mul(View view) {
        editText.append("*");
        str.append("*");
    }

    //clear
    public void empty(View view) {
        editText.setText(null);
        text.setText(null);
        str.delete(0, str.length());
    }

    //delete
    public void delete(View view) {
        String nowText = editText.getText().toString();
        if (nowText.length() != 0 && str.length() != 0) {
            editText.setText(nowText.substring(0, nowText.length() - 1));
            str.deleteCharAt(str.length() - 1);
        }
        text.setText(null);
    }

    //equal
    public void equal(View view) {
        indexYN = 0;
        estimate();
        if (indexYN == 0) {
            List<String> infixExpression = turnIntoInfixExpression(str.toString());
            System.out.println(infixExpression);
            List<String> suffixExpression = turnIntoSuffixExpression(infixExpression);
            System.out.println(suffixExpression);

            //String p = String.valueOf(infixExpression);
            String process = Restoreprocess((ArrayList<String>) infixExpression);
           /* for(int i = 1; i < infixExpression.size() ; i += 1){
                process = process + " " + infixExpression.get(i);
            }*/

            String result = String.valueOf(math(suffixExpression));

            String PR =  process + String.valueOf(" = ") + result;

            // Clear the editText content before appending the result
            editText.setText("");
            editText.append(result);

            String calculationPR = String.valueOf(PR);

            //Save the result to the database
            saveCalculationToDatabase(calculationPR);

            str.delete(0, str.length());
            str.append(result);
        }
    }

    private String Restoreprocess(ArrayList<String> process) {
        String ppp = "";
        for(int i = 0; i < process.size(); i += 1){
            String processi = process.get(i);
            if (processi.equals("g")) {
                ppp = ppp + "√";
            } else if (processi.equals("*0.01")){
                ppp = ppp + "%";
            } else if (processi.equals("p")) {
                ppp = ppp + "π";
            } else if (processi.equals("s")) {
                ppp = ppp + "sin";
            } else if (processi.equals("c")) {
                ppp = ppp + "cos";
            } else if (processi.equals("t")) {
                ppp = ppp + "tan";
            } else if (processi.equals("l")) {
                ppp = ppp + "ln";
            } else if (processi.equals("o")) {
                ppp = ppp + "log";
            }else{
                ppp = ppp + processi;
            }
        }
        return ppp;
    }


    //reciprocal
    public void reciprocal(View view) {
        editText.append("1/");
        str.append("1/");
    }

    //factorial
    public void factorial(View view) {
        editText.append("!");
        str.append("!");
    }

    //square
    public void square(View view) {
        editText.append("^2");
        str.append("^2");
    }
    //power
    public void power(View view) {
        editText.append("^");
        str.append("^");
    }

    //squareRoot
    public void squareRoot(View view) {
        editText.append("√");
        str.append("g");
    }

    //eulerNumber-e
    public void eulerNumber(View view) {
        editText.append("e");
        str.append("e");
    }

    //percentage
    public void percentage(View view) {
        editText.append("%");
        str.append("*0.01");
    }

    //PI
    public void pi(View view) {
        editText.append("π");
        str.append("p");
    }

    //sin
    public void sin(View view) {
        editText.append("sin");
        str.append("s");
    }

    //cos
    public void cos(View view) {
        editText.append("cos");
        str.append("c");
    }

    //tan
    public void tan(View view) {
        editText.append("tan");
        str.append("t");
    }

    //ln
    public void ln(View view) {
        editText.append("ln");
        str.append("l");
    }

    //log
    public void log(View view) {
        editText.append("log");
        str.append("o");
    }

    private List<String> turnIntoInfixExpression(String str) {
        //Convert the input string into an infix expression and store it in the list
        int index = 0;
        List<String> list = new ArrayList<>();
        do {
            char ch = str.charAt(index);
            if ("+-*/^!logsct()".indexOf(str.charAt(index)) >= 0) {
                //If it is the operator, directly added to the list
                index++;
                list.add(ch + "");
            } else if (str.charAt(index) == 'e' || str.charAt(index) == 'p') {
                index++;
                list.add(ch + "");
            } else if ("0123456789".indexOf(str.charAt(index)) >= 0) {
                //If it is a number, determine the case of multiple digits
                StringBuilder str1 = new StringBuilder();
                while (index < str.length()
                        && "0123456789.".indexOf(str.charAt(index)) >= 0) {
                    str1.append(str.charAt(index));
                    index++;
                }
                list.add(str1.toString());

            }
        } while (index < str.length());
        return list;
    }

    //Infix expression transformation is called suffix expression
    public List<String> turnIntoSuffixExpression(List<String> list) {
        Stack<String> fuZhan = new Stack<>();
        List<String> list2 = new ArrayList<>();
        if (!list.isEmpty()) {
            for (int i = 0; i < list.size(); i++) {
                if (isNumber(list.get(i))) {
                    list2.add(list.get(i));
                } else if (list.get(i).charAt(0) == '(') {
                    fuZhan.push(list.get(i));
                } else if (isOperator(list.get(i)) && list.get(i).charAt(0) != '(') {
                    if (fuZhan.isEmpty()) {
                        fuZhan.push(list.get(i));
                    } else {//The  stack is not empty
                        if (list.get(i).charAt(0) != ')') {
                            if (adv(fuZhan.peek()) <= adv(list.get(i))) {
                                //Store in the stack
                                fuZhan.push(list.get(i));
                            } else {
                                //Take out the stack
                                while (!fuZhan.isEmpty() && !"(".equals(fuZhan.peek())) {
                                    if (adv(list.get(i)) <= adv(fuZhan.peek())) {
                                        list2.add(fuZhan.pop());
                                    }
                                }
                                if (fuZhan.isEmpty() || fuZhan.peek().charAt(0) == '(') {
                                    fuZhan.push(list.get(i));
                                }
                            }
                        } else if (list.get(i).charAt(0) == ')') {
                            while (fuZhan.peek().charAt(0) != '(') {
                                list2.add(fuZhan.pop());
                            }
                            fuZhan.pop();
                        }
                    }
                }
            }
            while (!fuZhan.isEmpty()) {
                list2.add(fuZhan.pop());
            }
        } else {
            editText.setText("");
        }
        return list2;
    }

    //Determine whether it is an operator
    public static boolean isOperator(String op) {
        return "0123456789.ep".indexOf(op.charAt(0)) == -1;
    }

    //Determine whether it is an operand.
    public static boolean isNumber(String num) {
        return "0123456789ep".indexOf(num.charAt(0)) >= 0;
    }

    //Determine the priority of the operator
    public static int adv(String f) {
        int result = 0;
        switch (f) {
            case "+":
            case "-":
                result = 1;
                break;
            case "*":
            case "/":
                result = 2;
                break;
            case "^":
                result = 3;
                break;
            case "!":
            case "g":
            case "l":
            case "o":
            case "s":
            case "c":
            case "t":
                result = 4;
                break;
        }
        return result;
    }
    //Calculated by suffix expression
    public double math(List<String> list2) {
        Stack<String> stack = new Stack<>();
        for (int i = 0; i < list2.size(); i++) {
            if (isNumber(list2.get(i))) {
                if (list2.get(i).charAt(0) == 'e') {
                    stack.push(String.valueOf(Math.E));
                } else if (list2.get(i).charAt(0) == 'p') {
                    stack.push(String.valueOf(Math.PI));
                } else {
                    stack.push(list2.get(i));
                }
            } else if (isOperator(list2.get(i))) {
                double res = 0;
                switch (list2.get(i)) {
                    case "+": {
                        double num2 = Double.parseDouble(stack.pop());
                        double num1 = Double.parseDouble(stack.pop());
                        res = num1 + num2;
                        break;
                    }
                    case "-": {
                        double num2 = Double.parseDouble(stack.pop());
                        double num1 = Double.parseDouble(stack.pop());
                        res = num1 - num2;
                        break;
                    }
                    case "*": {
                        double num2 = Double.parseDouble(stack.pop());
                        double num1 = Double.parseDouble(stack.pop());
                        res = num1 * num2;
                        break;
                    }
                    case "/": {
                        double num2 = Double.parseDouble(stack.pop());
                        double num1 = Double.parseDouble(stack.pop());
                        if (num2 != 0) {
                            res = num1 / num2;
                        } else {
                            text.setText("Can't be divided by 0!");
                            indexYN = 1;
                        }
                        break;
                    }
                    case "^": {
                        double num2 = Double.parseDouble(stack.pop());
                        double num1 = Double.parseDouble(stack.pop());
                        res = Math.pow(num1, num2);
                        break;
                    }
                    case "!": {
                        double num1 = Double.parseDouble(stack.pop());
                        if (num1 == 0 || num1 == 1) {
                            res = 1;
                        } else if (num1 == (int) num1 && num1 > 1) {
                            int d = 1;
                            for (int j = (int) num1; j > 0; j--) {
                                d *= j;
                            }
                            res = d;
                        } else {
                            text.setText("The factorial must be a natural number!");
                            indexYN = 1;
                        }
                        break;
                    }
                    case "g": {
                        double num1 = Double.parseDouble(stack.pop());
                        res = Math.sqrt(num1);
                        break;
                    }
                    case "l": {
                        double num1 = Double.parseDouble(stack.pop());
                        if (num1 > 0) {
                            res = Math.log(num1);
                        } else {
                            text.setText("The variable x in ln must be greater than 0!");
                            indexYN = 1;
                        }
                        break;
                    }
                    case "o": {
                        double num1 = Double.parseDouble(stack.pop());
                        if (num1 > 0) {
                            res = Math.log(num1) / Math.log(2);
                        } else {
                            text.setText("The variable x in function log must be greater than 0!");
                            indexYN = 1;
                        }
                        break;
                    }
                    case "s": {
                        double num1 = Double.parseDouble(stack.pop());
                        res = Math.sin(num1);
                        break;
                    }
                    case "c": {
                        double num1 = Double.parseDouble(stack.pop());
                        res = Math.cos(num1);
                        break;
                    }
                    case "t": {
                        double num1 = Double.parseDouble(stack.pop());
                        if (Math.cos(num1) != 0) {
                            res = Math.tan(num1);
                        } else {
                            text.setText("The variable in function tan can't be +-(π/2 + kπ)!");
                            indexYN = 1;
                        }
                        break;
                    }
                }
                stack.push("" + res);
            }
        }
        if (indexYN == 0) {
            if (!stack.isEmpty()) {
                return Double.parseDouble(stack.pop());
            } else {
                return 0;
            }
        } else {
            return -999999;
        }
    }

    //To determine whether the input is wrong
    public void estimate() {
        int i;
        if (str.length() == 0) {
            text.setText("Error!");
            indexYN = 1;
        }
        if (str.length() == 1) {
            //When there is only one character, it can only be one of ' 0123456789ep'
            if ("0123456789ep".indexOf(str.charAt(0)) == -1) {
                text.setText("Error!");
                indexYN = 1;
            }
        }
        if (str.length() > 1) {
            for (i = 0; i < str.length() - 1; i++) {
                //1.The first character can only be one of ' losctg ( 0123456789ep ' )
                if ("losctg(0123456789ep".indexOf(str.charAt(0)) == -1) {
                    text.setText("Error!");
                    indexYN = 1;
                }
                //2. ' + - * / ' can only be followed by ' 0123456789losctg ( ep ' )
                if ("+-*/".indexOf(str.charAt(i)) >= 0
                        && "0123456789losctg(ep".indexOf(str.charAt(i + 1)) == -1) {
                    text.setText("Error!");
                    indexYN = 1;
                }
                //3. '. ' behind can only be one of ' 0123456789'
                if (str.charAt(i) == '.' && "0123456789".indexOf(str.charAt(i + 1)) == -1) {
                    text.setText("Error!");
                    indexYN = 1;
                }
                //4. ' ! After '  ' is only one of ' + - * / ^ ) '
                if (str.charAt(i) == '!' && "+-*/^)".indexOf(str.charAt(i + 1)) == -1) {
                    text.setText("Error!");
                    indexYN = 1;
                }
                //5.Behind ' losctg ' is only one of ' 0123456789 ( ep ' )
                if ("losctg".indexOf(str.charAt(i)) >= 0 && "0123456789(ep".indexOf(str.charAt(i + 1)) == -1) {
                    text.setText("Error!");
                    indexYN = 1;
                }
                //6.Judgment operation of  ' 0'
                if (str.charAt(0) == '0' && str.charAt(1) == '0') {
                    text.setText("Error!");
                    indexYN = 1;
                }
                if (i >= 1 && str.charAt(i) == '0') {
                    //&& str.charAt(0) == '0' && str.charAt(1) == '0'
                    int n = i;
                    int is = 0;
                    //1.When the previous character of 0 is not ' 0123456789. ', the latter character can only be ' + - * /. ! One of ^ ) '
                    if ("0123456789.".indexOf(str.charAt(i - 1)) == -1 && "+-*/.!^)".indexOf(str.charAt(i + 1)) == -1) {
                        text.setText("Error!");
                        indexYN = 1;
                    }
                    //If the previous position of 0 is '. ', the latter position can only be one of ' 0123456789 + - * /. ^ ) '.
                    if (str.charAt(i - 1) == '.' && "0123456789+-*/.^)".indexOf(str.charAt(i + 1)) == -1) {
                        text.setText("Error!");
                        indexYN = 1;
                    }
                    n -= 1;
                    while (n > 0) {
                        if ("(+-*/^glosct".indexOf(str.charAt(n)) >= 0) {
                            break;
                        }
                        if (str.charAt(n) == '.') {
                            is++;
                        }
                        n--;
                    }

                    //If there is no '. ' between 0 and the previous operator, the first integer bit can only be ' 123456789 ', and the latter can only be ' 0123456789 + - * /. ! ^ ) ' one of them.
                    if ((is == 0 && str.charAt(n) == '0') || "0123456789+-*/.!^)".indexOf(str.charAt(i + 1)) == -1) {
                        text.setText("Error!");
                        indexYN = 1;
                    }
                    //If there is a '. ' between 0 and the previous operator, the latter one can only be one of the ' 0123456789 + - * /. ^ ) '
                    if (is == 1 && "0123456789+-*/.^)".indexOf(str.charAt(i + 1)) == -1) {
                        text.setText("Error!");
                        indexYN = 1;
                    }
                    if (is > 1) {
                        text.setText("Error!");
                        indexYN = 1;
                    }

                }
                //7. ' 123456789 ' can only be followed by ' 0123456789 + - * /. ! One of ^ ) '
                if ("123456789".indexOf(str.charAt(i)) >= 0 && "0123456789+-*/.!^)".indexOf(str.charAt(i + 1)) == -1) {
                    text.setText("Error!");
                    indexYN = 1;
                }
                //8. ' ( ' After ' is only one of ' 0123456789locstg ( ) ep' )
                if (str.charAt(i) == '(' && "0123456789locstg()ep".indexOf(str.charAt(i + 1)) == -1) {
                    text.setText("Error!");
                    indexYN = 1;
                }
                //9. ' ) ' can only be followed by ' + - * / ! One of ^ ) '
                if (str.charAt(i) == ')' && "+-*/!^)".indexOf(str.charAt(i + 1)) == -1) {
                    text.setText("Error!");
                    indexYN = 1;
                }
                //The last character can only be ' 0123456789 ! ) one of ep '
                if ("0123456789!)ep".indexOf(str.charAt(str.length() - 1)) == -1) {
                    text.setText("Error!");
                    indexYN = 1;
                }
                //11. There cannot be more than one '. '
                if (i > 2 && str.charAt(i) == '.') {
                    int n = i - 1;
                    int is = 0;
                    while (n > 0) {
                        if ("(+-*/^glosct".indexOf(str.charAt(n)) >= 0) {
                            break;
                        }
                        if (str.charAt(n) == '.') {
                            is++;
                        }
                        n--;
                    }
                    if (is > 0) {
                        text.setText("Error!");
                        indexYN = 1;
                    }
                }
                //12. ' ep ' can only be followed by ' + - * / ^ ) '.
                if ("ep".indexOf(str.charAt(i)) >= 0 && "+-*/^)".indexOf(str.charAt(i + 1)) == -1) {
                    text.setText("Error!");
                    indexYN = 1;
                }
            }
        }
    }
}
