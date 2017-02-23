package dance.dance;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.text.Layout;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.view.Window;
import android.view.WindowManager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import org.apache.commons.net.ftp.*;
import org.w3c.dom.Text;

import java.net.*;
import java.util.Calendar;
import java.util.Collections;
import java.util.Locale;
import java.util.Random;
import java.util.Stack;


public class MainActivity extends Activity {
    static String input;
    ListView nominationList;
    ArrayList<String> strs=new ArrayList<>();
    ArrayList<String> judges=new ArrayList<>();
    ArrayList<String> sha=new ArrayList<>();
    ArrayList<String> dord=new ArrayList<>();
    byte[] shas;
    String finsha;
    String[] gruppa;
    String[] t_parol=new String[21];
    int strt,end,tek,tc;
    int[] judg_nums=new int[21];
    String t_nomination, t_judge,t_key;
    ArrayList<String> red=new ArrayList<>();
    Integer nomination_num, yMarks,yMarksDone, round,pairs,turnCount,turnNumber,battery_lvl,judge_num,nfLayout;
    int state = 0,size,startButton,finAmount,totalAmount,declined,posCounters, danceNumber, danceCount;
    int mask=4;
    boolean auto=true;
    MainActivity self = this;
    boolean paroled = false;
    boolean error=false;
    boolean isSha=false;
    boolean lostconncetion=false;
    boolean debug=true;
    ArrayList<String[]> pairsNum=new ArrayList<>();
    ArrayList<Integer[]> pairsState=new ArrayList<>();
    ArrayList<String> addPairs=new ArrayList<>();
    int[] marksDone=new int[9];
    int[] fbutton={R.id.button18,R.id.button3,R.id.button31,R.id.button51,R.id.button89,R.id.button98,R.id.button107,R.id.button116,R.id.button125};
    int[] flines={R.id.fl1,R.id.fl2,R.id.fl3,R.id.fl4,R.id.fl5,R.id.fl6,R.id.fl7,R.id.fl8,R.id.fl9};
    String path = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
    File sdPath,nomPath,resPath;
    File backup,logger,config,logger2;
    BufferedWriter bWriter,logWriter;
    Connecter c=new Connecter();
    boolean restore,lang,newinit;
    Thread th=null;
    Random rnd=new Random();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                battery_lvl = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            }
        };
        registerReceiver(receiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Init();
        setContentView(R.layout.language);
        CreateLangListners();
        DisplayCharge();
    }

    private void recurseMain(){CreteMainListeners();}

    private void Init() {
        sdPath = new File(path);
        if(!sdPath.mkdirs())log("Cannot create main folder");
        backup =new File(path,"backup.log");
        logger= new File(path,"log.txt");
        config= new File(path,"cfg.txt");
        ReadSettings();
        logger2= new File(path,"alllog.txt");
        log("New session..........................................");
        String h,u,p;
        System.setProperty("line.separator", "\r\n");
        try {
            logWriter = new BufferedWriter(new FileWriter(logger));
            if (config.exists()) {
                BufferedReader br = new BufferedReader(new FileReader(config));
                h = br.readLine();
                u = br.readLine();
                p = br.readLine();
                log("Config found");
                br.close();
            }else {
                h="192.168.0.13";
                u="test";
                p="1234";
                log("Config setted to default");
            }
            for(int i=0;i<6;++i)marksDone[i]=0;
            c.setGlobals(h, u, p);
            c.setLog(logWriter);
            c.init(this);
        }catch(Exception e){log("Error in Init:"+e.getMessage());}
        Connect();
        c.startReconnect(this);
        log("Init completed");
        lang=false;
        newinit=false;
    }

    private void Connect(){
        if(!isOnline()){
            error=true;
            lostconncetion=true;
            log("No connection");
            return;
        }
        c.setState(1);
        try {
            int countdown=100;
            while((!c.Done())&&(countdown!=0)) {
                Thread.sleep(100);
                countdown--;
            }
            if(countdown==0){
                error=true;
                lostconncetion=true;
                logWriter.write("Time to connect has expired"+'\n');
                log("Time to connect has expired");
            }
            else{
                error=false;
                lostconncetion=false;
                log("Conection has established");
            }
            Thread.sleep(rnd.nextInt(3000));
            c.setState(8);
            SynWait(5);
        } catch (Exception e) {log("Error detected in Connect:"+e.getMessage());}
    } 

    private void CreteMainListeners(){
        log("Creating main menu");
        nomination_num=-1;
        nominationList = (ListView) findViewById(R.id.nominationList);
        nominationList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        findViewById(R.id.next).setEnabled(!error);
        error=lostconncetion;
        if(!error) {
            log("No errors detected");
            try {
                File sdFile = new File(sdPath, "tnominations.txt");
                c.setDFile("nominations.txt");
                c.setDPath("/airdance");
                c.setFile(sdFile);
                c.setState(2);
                log("Download nomination list");
                SynWait(5);
                log("Download completed");
                strs.clear();
                String tmp;
                BufferedReader br = new BufferedReader(new FileReader(sdFile));
                while ((tmp = br.readLine()) != null) {
                    //if(tmp.length()>28)
                     //   tmp=tmp.substring(0,27);
                    strs.add(tmp);
                    log("New nomination:"+tmp);
                }
            } catch (FileNotFoundException e) {
                log("File not found");
                e.printStackTrace();
            } catch (IOException e) {
                log("File can't be readed");
                e.printStackTrace();
            }
        }
        else {
            log("Errors detected. No connection");
            strs.clear();
            strs.add(0,"Ошибка подключения");
            strs.add(1, "Проверьте наличие сети и");
            strs.add(2, "доступность хранилища");
        }
        DeleteTmp();
        ArrayAdapter<String> nominationCount = new ArrayAdapter<String>(this,R.layout.list, strs)
        {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text = (TextView) view.findViewById(android.R.id.text1);
                text.setMaxLines(1);
                text.setTypeface(null, Typeface.BOLD);
                text.setTextSize(28);
                text.setBackgroundResource(R.color.material_grey_300);
                return view;
            }

        };

        nominationList.setAdapter(nominationCount);
        if(!error) {
            nominationList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View itemClicked, int position, long id) {
                    if (strs.get(position).length() > 2) {
                        log(position+" nomination clicked");
                        int s = (strs.get(position).charAt(0) == '\uFEFF') ? 1 : 0;
                        int t = strs.get(position).indexOf(":");
                        String val = strs.get(position).substring(s, t);
                        nomination_num = Integer.valueOf(val);
                        log(nomination_num+" nom has chosen");
                        nomPath=new File(path+"/"+val);
                        if(!nomPath.mkdir())log("Directory for nomination cannot be created");
                        if(!(new File(path+"/"+val+"/results")).mkdir())log("Directory for results cannot be created");
                        t_nomination = strs.get(position).substring(t + 1);
                        for(int i=0;i<nominationList.getChildCount();++i)
                            nominationList.getChildAt(i).setBackgroundResource(R.color.material_grey_300);
                        itemClicked.setBackgroundResource(R.color.selected);
                    }
                }
            });
        }
        Button bck = (Button) findViewById(R.id.exit);
        bck.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View v) {
                try {
                    logWriter.close();
                    c.setState(4);
                    Thread.sleep(100);
                    c.setState(-1);
                    Thread.sleep(100);
                } catch (Exception e) {log("Exception in button.Exit:"+e.getMessage());}
                log("************App is closing****************");
                self.finish();
            }
        });
        bck = (Button) findViewById(R.id.refresh);
        bck.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View v) {
                log("Refresh inited");
                if(error)
                    Connect();
                recurseMain();
            }
        });
        bck = (Button) findViewById(R.id.next);
        bck.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if ((nomination_num > 0) && (t_nomination.length() > 2)) {
                    log("Enter in " + nomination_num + " nomination");
                    setContentView(R.layout.second_activ);
                    state = 1;
                    ReadNomination();
                    ReadJudges();
                    if(lostconncetion){
                        log("Connection lost in nomination entering");
                        setContentView(R.layout.activity_main);
                        state=0;
                        recurseMain();
                        return;
                    }
                    CreateJudgeListeners();
                }
            }
        });
        bck = (Button) findViewById(R.id.resque);
        bck.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View v) {
                setContentView(R.layout.keyboard);
                t_key = "";
                ((EditText) findViewById(R.id.txt)).setText(t_key);
                mask = 4;
                auto = false;
                CreateKeyBoard();
                Button bck = (Button) findViewById(R.id.bo);
                bck.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        switch (t_key) {
                            case "2222":
                                setContentView(R.layout.settings);
                                CreateSettingsListeners();
                                break;
                            case "":
                                ((EditText) findViewById(R.id.txt)).setText(t_key);
                                break;
                            default:
                                ((EditText) findViewById(R.id.txt)).setText(getResources().getString(R.string.wrongkey));
                                t_key = "";
                                break;
                        }
                    }
                });
                bck = (Button) findViewById(R.id.key_back);
                bck.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                            setContentView(R.layout.activity_main);
                            state = 0;
                            recurseMain();
                        }
                });
            }
        });
        TextView charge = (TextView) findViewById(R.id.charge);
        if(charge!=null) {
            charge.setText(String.format("%d%%", battery_lvl));
            if(battery_lvl>60)charge.setBackgroundResource(R.color.tvYes);
            else if(battery_lvl>30)charge.setBackgroundResource(R.color.tvMb);
            else charge.setBackgroundResource(R.color.tvRed);
        }
        log("Main menu created");
    }

    private void ReadJudges() {
        try {
            log("Read Judges started");
            File sdFile = new File(sdPath, "tjudges.txt");
            sdFile.delete();
            c.setDFile("judge.txt");
            c.setDPath("/airdance/" + String.valueOf(nomination_num));
            c.setFile(sdFile);
            c.setState(2);
            lostconncetion=SynWait(4);
            if(lostconncetion){log("Connection lost while reading judges"); return;}
            if(!sdFile.exists()) {
                lostconncetion=true;
                return;
            }
            BufferedReader br =new BufferedReader(new FileReader(sdFile));
            int i = 0,j = 0;
            String str;
            judges.clear();
            for(int n=0;n<judg_nums.length;++n){
                judg_nums[n]=-1;
            }
            c.strs.clear();
            c.setCPath("/airdance/" + String.valueOf(nomination_num) + "/judges/*.lock");
            c.setState(9);
            lostconncetion=SynWait(2);
            String tour=(round<10?"0":"")+round;
            c.setCPath("/airdance/" + String.valueOf(nomination_num) + "/results/t"+tour+"*.lock");
            c.setState(9);
            lostconncetion=SynWait(2);
            if(lostconncetion)log("Connection lost while checking files");
            if(lostconncetion){Connect();if(!lostconncetion){ReadJudges();return;}}
            while ((str = br.readLine()) != null) {
                log("Read judge on place "+(i+1)+":"+str);
                if (str.length() < 2) continue;
                if ((str.indexOf(";") < 3) && (str.contains(";"))) continue;
                str = str.substring(str.charAt(0) == '\uFEFF' ? 2 : 1, str.length() - 1);
                int end;
                if (str.contains(";")) end = str.indexOf(";") - 1;
                else end = str.length();
                if (str.equals("")) continue;
                judges.add(Integer.toString(i + 1) + ".  " + str.substring(0, end));
                if (!exists(i+1)) {
                    judg_nums[j] = i;
                    j++;
                }
                if (str.contains(";")) {
                    str = str.substring(end + 3);
                    t_parol[i] = str.substring(0, str.indexOf("\""));
                    paroled = true;
                } else
                    paroled = false;
                i++;
            }
            log("Read Judges ended with success;paroled="+String.valueOf(paroled));
        } catch (Exception e) {
            log("Exception in read judges"+e.getMessage());
        }
    }

    private boolean exists(int x){
        boolean tmp=false;
        for(int i=0;i<c.strs.size();++i) {
            int n=c.strs.get(i).indexOf(".");
            int v=Integer.valueOf(c.strs.get(i).substring(n-2,n));
            if (v == x)
                tmp = true;
        }
        log(x+" file"+(tmp?"":" doesn't")+" exists");
        return tmp;
    }

    private void CreateJudgeListeners() {
        findViewById(R.id.sa_no).setVisibility(View.GONE);
        findViewById(R.id.sa_yes).setVisibility(View.GONE);
        findViewById(R.id.desc).setVisibility(View.GONE);
        findViewById(R.id.sa_3).setVisibility(View.GONE);
        findViewById(R.id.sa_2).setVisibility(View.VISIBLE);
        findViewById(R.id.sa_1).setVisibility(View.VISIBLE);
        log("CreateJudgeListeners started");
        ArrayAdapter<String> judgeList = new ArrayAdapter<String>(self, R.layout.list, judges)
        {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text = (TextView) view.findViewById(android.R.id.text1);
                text.setMaxLines(2);
                text.setTypeface(null, Typeface.BOLD);
                text.setTextSize(28);
                boolean entered=true;
                for (int judg_num : judg_nums)
                    if (judg_num == position)
                        entered = false;
                text.setTextColor(entered ? Color.rgb(100, 100, 100) : Color.rgb(0, 0, 0));
                text.setBackgroundResource(R.color.material_grey_300);
                return view;
            }
        };
        ListView judge_lv=((ListView) findViewById(R.id.judges));
        judge_lv.setAdapter(judgeList);
        judge_lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View itemClicked, int position, long id) {
                judge_num = position + 1;
                log(judge_num + " judge clicked");
                if (judges.get(position).length() > 2)
                    t_judge = judges.get(position).substring(4);
                ListView judge_lv=((ListView) findViewById(R.id.judges));
                for(int i=0;i<judge_lv.getChildCount();++i)
                    judge_lv.getChildAt(i).setBackgroundResource(R.color.material_grey_300);
                itemClicked.setBackgroundResource(R.color.selected);
            }
        });
        ((TextView)findViewById(R.id.jnom)).setText(t_nomination.length() > 27 ? t_nomination.substring(0, 27) : t_nomination);
        Button judgeOk = (Button) findViewById(R.id.next);
        judgeOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (t_judge == null) return;
                c.strs.clear();
                c.setCPath("/airdance/" + String.valueOf(nomination_num) + "/judges/*.lock");
                c.setState(9);
                lostconncetion = SynWait(2);
                String tour = (round< 10 ? "0" : "") + round;
                c.setCPath("/airdance/" + String.valueOf(nomination_num) + "/results/t" + tour + "*.lock");
                c.setState(9);
                lostconncetion = SynWait(2);
                if (exists(judge_num)) {
                    return;
                }
                        if (!paroled) {
                            Button bck1 = (Button) findViewById(R.id.sa_yes);
                            findViewById(R.id.sa_2).setVisibility(View.GONE);
                            bck1.setVisibility(View.VISIBLE);
                            findViewById(R.id.desc).setVisibility(View.VISIBLE);
                            findViewById(R.id.sa_3).setVisibility(View.VISIBLE);
                            ((TextView) findViewById(R.id.desc)).setText(String.format(getResources().getString(R.string.r_u),t_judge));
                            findViewById(R.id.sa_no).setVisibility(View.VISIBLE);
                            findViewById(R.id.sa_no).setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                    findViewById(R.id.sa_no).setVisibility(View.GONE);
                                    findViewById(R.id.sa_yes).setVisibility(View.GONE);
                                    findViewById(R.id.desc).setVisibility(View.GONE);
                                    findViewById(R.id.sa_3).setVisibility(View.GONE);
                                    findViewById(R.id.sa_2).setVisibility(View.VISIBLE);
                                    findViewById(R.id.sa_1).setVisibility(View.VISIBLE);
                                }
                            });
                            findViewById(R.id.sa_1).setVisibility(View.GONE);
                            bck1.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                            danceNumber = 1;
                            restore = CheckJudge();
                            SendLock(false);
                            if (round != 1) {
                                ReadDance();
                            } else {
                                setContentView(R.layout.fin);
                                state = 4;
                                ReadDanceF();
                                SendInfo();
                                CreateEventsF();
                            }
                                }
                            });
                        } else {
                            setContentView(R.layout.keyboard);
                            t_key = "";
                            ((EditText) findViewById(R.id.txt)).setText(t_key);
                            mask = 4;
                            auto = true;
                            CreateKeyBoard();
                            Button bck = (Button) findViewById(R.id.bo);
                            bck.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                    if ((t_parol[judge_num-1].equals(t_key)) || (t_key.equals("2222"))) {
                                        danceNumber = 1;
                                        restore = CheckJudge();
                                        SendLock(false);
                                        if (round != 1) {
                                            ReadDance();
                                        } else {
                                            setContentView(R.layout.fin);
                                            state = 4;
                                            ReadDanceF();
                                            SendInfo();
                                            CreateEventsF();
                                        }
                                    } else if (t_key.equals(""))
                                        ((EditText) findViewById(R.id.txt)).setText(t_key);
                                    else {
                                        ((EditText) findViewById(R.id.txt)).setText(getResources().getString(R.string.wrongkey));
                                        t_key = "";
                                    }
                                }
                            });
                            bck = (Button) findViewById(R.id.key_back);
                            bck.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                    if ((nomination_num > 0) && (t_nomination.length() > 2)) {
                                        setContentView(R.layout.second_activ);
                                        state = 1;
                                        CreateJudgeListeners();
                                    }
                                }
                            });
                        }

            }
        });
        Button bck = (Button) findViewById(R.id.back);
        bck.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View v) {
                state = 0;
                setContentView(R.layout.activity_main);
                recurseMain();
            }
        });
        bck = (Button) findViewById(R.id.s_back);
        bck.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View v) {
                state = 0;
                setContentView(R.layout.activity_main);
                recurseMain();
            }
        });
        bck = (Button) findViewById(R.id.jrefresh);
        bck.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View v) {
                log("Start to refresh judge list");
                ReadNomination();
                ReadJudges();
                if(lostconncetion){
                    log("Connection lost in judge refreshing");
                    setContentView(R.layout.activity_main);
                    state=0;
                    recurseMain();
                    return;
                }
                CreateJudgeListeners();
            }
        });
        log("CreateJudgeListeners ended successfully");
    }

    private boolean CheckJudge(){
        boolean q;
        try{
            q=c.exists("/airdance/"+String.valueOf(nomination_num)+"/judges",Integer.toString(round)+"_"+Integer.toString(judge_num)+".txt");
            if(q) {
                File sdFile = new File(sdPath, "tbackup.txt");
                c.setDFile(Integer.toString(round) + "_" + Integer.toString(judge_num) + ".txt");
                c.setDPath("/airdance/" + String.valueOf(nomination_num) + "/judges");
                c.setFile(sdFile);
                c.setState(2);
                SynWait(4);
                BufferedReader br =new BufferedReader(new FileReader(sdFile));
                String str=br.readLine();
                danceNumber=Integer.valueOf(str.substring(0,str.indexOf(":")));
                turnNumber=Integer.valueOf(str.substring(str.indexOf(":")+1,str.length()))-1;
                br.close();
            }
        }catch(Exception e){log("In CHeckJudge excpetion:"+e.getMessage());q=false;}
        return q;
    }

    private void ReadNomination() {
        try {
            File sdFile = new File(sdPath, "tgroup.txt");
            c.setDFile("gruppa.txt");
            c.setDPath("/airdance/" + String.valueOf(nomination_num));
            c.setFile(sdFile);
            c.setState(2);
            lostconncetion=SynWait(4);
            if(lostconncetion)return;
            BufferedReader br =new BufferedReader(new FileReader(sdFile));
            String str = br.readLine().substring(1);
            gruppa = str.split(";");
            t_nomination=gruppa[0].substring(gruppa[0].indexOf("\"") + 1, gruppa[0].lastIndexOf("\""));
            pairs=Integer.valueOf(gruppa[1]);
            yMarks=Integer.valueOf(gruppa[2]);
            round=Integer.valueOf(gruppa[3]);
            danceCount=Integer.valueOf(gruppa[4]);
            br.close();
            red.clear();
            for(int j=1;j<=danceCount;++j) {
                sdFile = new File(sdPath, "tdance" + Integer.toString(j) + ".txt");
                if (j < 10)
                    c.setDFile("dance0" + Integer.toString(j) + ".txt");
                else
                    c.setDFile("dance" + Integer.toString(j) + ".txt");
                c.setDPath("/airdance/" + String.valueOf(nomination_num));
                c.setFile(sdFile);
                c.setState(2);
                lostconncetion=SynWait(4);
                if(lostconncetion)return;
            }
            for(int j=5;j<gruppa.length;++j)gruppa[j]=gruppa[j].substring(1,gruppa[j].length()-1);
            isSha=false;
            if(!c.exists("/airdance/"+String.valueOf(nomination_num),"sha1.txt"))
                return;
            isSha=true;
            sdFile = new File(sdPath, "tsha1.txt");
            c.setDFile("sha1.txt");
            c.setDPath("/airdance/" + String.valueOf(nomination_num));
            c.setFile(sdFile);
            c.setState(2);
            lostconncetion=SynWait(4);
            if(lostconncetion)return;
            br =new BufferedReader(new FileReader(sdFile));
            while((str = br.readLine())!=null)
                dord.add(str.substring(str.indexOf("_")+1,str.indexOf(".")));
            br.close();
        } catch (Exception e) {
            log("Catch exception in ReadNomin:"+e.getMessage());
        }
    }

    private int ReadDance() {
        int totalCount=0;
        try {
            addPairs.clear();
            turnCount=0;
            yMarksDone=0;
            pairsNum.clear();
            pairsState.clear();
            int max=0;
            File sdFile = new File(sdPath, "tdance" + Integer.toString(danceNumber) + ".txt");
            BufferedReader br =new BufferedReader(new FileReader(sdFile));
            String str;
            while ((str = br.readLine()) != null) {
                String[] tmp = str.split(";");
                int j=0;
                while((j<tmp.length)&&(!tmp[j].equals("0")))j++;
                totalCount+=j-1;
                max=max>j-1?max:j-1;
                tmp[0]=tmp[0].substring(tmp[0].charAt(0) == '\uFEFF' ? 2 : 1, tmp[0].length() - 1);
                pairsNum.add(tmp);
                Integer[] x=new Integer[25];
                for(int i=0;i<25;++i)x[i]=0;
                pairsState.add(x);
                turnCount++;
            }
            br.close();
            strt=1;
            end=turnCount-4;
            if(end<=0)end=1;
            tek=1;
            declined=0;
            if(restore){
                try{
                    restore = false;
                    String check="t0" + Integer.toString(round) + ((judge_num < 10) ? "j0" : "j") + Integer.toString(judge_num) + "_" + gruppa[danceNumber + 4] + ".txt";
                    if(c.exists("/airdance/" + String.valueOf(nomination_num) + "/results",check)) {
                        sdFile = new File(sdPath, "tres.txt");
                        c.setDFile(check);
                        c.setDPath("/airdance/" + String.valueOf(nomination_num) + "/results");
                        c.setFile(sdFile);
                        c.setState(2);
                        SynWait(4);
                        br = new BufferedReader(new FileReader(sdFile));
                        while ((str = br.readLine()) != null)
                            setPair(str);
                        br.close();
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
            else
                turnNumber=0;
            if(max>19) {
                setContentView(R.layout.prom);
                nfLayout=R.layout.prom;
                size = 25;
                startButton=R.id.button;
            }
            else {
                setContentView(R.layout.prom16);
                nfLayout=R.layout.prom16;
                size=20;
                startButton=R.id.button61;
            }
            CreateEventsNF();
            state = 3;
            SetRed();
            SendInfo();
            FillTitles(totalCount);
            FillPairs();
        } catch (Exception e) {
            log("Exception in ReadDance:"+e.getMessage());
        }
        return totalCount;
    }

    private boolean setPair(String pairnum){
        boolean q=false;
        for(int i=0;i<pairsNum.size();++i)
            for(int j=1;j<pairsNum.get(i).length;++j)
                if(pairsNum.get(i)[j].equals(pairnum)) {
                    q=true;
                    if(pairsState.get(i)[j - 1] != 1) {
                        pairsState.get(i)[j - 1] = 1;
                        yMarksDone++;
                    }
                }
        return q;
    }

    private void FillTitles(int totalCount){
        totalAmount=totalCount;
        ((TextView)findViewById(R.id.desc_fam)).setText(String.format("%s. %s", Integer.toString(judge_num), t_judge));
        ((TextView)findViewById(R.id.desc)).setText(String.format("1/%s %s", Integer.toString(pow(round)), t_nomination));
        ((TextView)findViewById(R.id.desc2)).setText(getResources().getString(R.string.Heats));
        ((TextView)findViewById(R.id.desc3)).setText(String.format("%s;", Integer.toString(turnCount)));
        ((TextView)findViewById(R.id.desc4)).setText(String.format("%s->%s", Integer.toString(totalCount), Integer.toString(yMarks)));
        ((TextView)findViewById(R.id.counter)).setText(Integer.toString(yMarksDone));
        ((TextView)findViewById(R.id.dance)).setText((pairsNum.get(turnNumber)[0]));
        for(int i=0;i<5;++i){
            findViewById(R.id.button55+i).setVisibility(i < pairsNum.size() ? View.VISIBLE : View.INVISIBLE);
        }
        findViewById(R.id.button54).setEnabled(false);
        findViewById(R.id.button60).setEnabled(turnNumber>5);
    }

    private boolean Questioned(int turn){
        turn--;
        boolean f=false;
        if(turn>=turnCount)return false;
        for(int i=0;i<pairsState.get(turn).length;++i)
            if(pairsState.get(turn)[i]==2)
                f=true;
        return f;
    }

    private void FillPairs(){
        log("Fill pairs{dance:"+danceNumber+";turn:"+turnNumber+";Ymarks:"+posCounters+";Declined:"+declined+";}");
        findViewById(R.id.button54).setEnabled(!(tek == strt));
        findViewById(R.id.button60).setEnabled(!(tek >= end));
        if(declined+yMarks==totalAmount){for(int i=0;i<pairsState.size();i++)for(int j=0;j<size;j++)
            if((pairsState.get(i)[j]<3)&&(!pairsNum.get(i)[j+1].equals("0")))
                pairsState.get(i)[j]+=3;}
        else for(int i=0;i<pairsState.size();i++)for(int j=0;j<size;j++)if((pairsState.get(i)[j]>2)&&(pairsState.get(i)[j]<6))pairsState.get(i)[j]-=3;
        for(int i=0;i<size;++i) {
            String s=((i+1)<pairsNum.get(turnNumber).length)?pairsNum.get(turnNumber)[i + 1]:"0";
            if(s.equals("0")) {
                ((Button) findViewById(startButton+i)).setText("");
                findViewById(startButton+i).setVisibility(View.INVISIBLE);
            }
            else {
                ((Button) findViewById(startButton + i)).setText(pairsNum.get(turnNumber)[i + 1]);
                findViewById(startButton+i).setVisibility(View.VISIBLE);
            }
            if(pairsState.get(turnNumber)[i]==1)findViewById(startButton + i).setBackgroundResource(R.color.tvYes);
            else if(pairsState.get(turnNumber)[i]==4)findViewById(startButton + i).setBackgroundResource(R.color.tvYes);
            else if(pairsState.get(turnNumber)[i]==2)findViewById(startButton + i).setBackgroundResource(R.color.tvMb);
            else if(pairsState.get(turnNumber)[i]==5)findViewById(startButton + i).setBackgroundResource(R.color.tvYes);
            else if(pairsState.get(turnNumber)[i]==0)findViewById(startButton + i).setBackgroundResource(android.R.color.background_light);
            else if(pairsState.get(turnNumber)[i]==3)findViewById(startButton + i).setBackgroundResource(R.color.tvYes);
            else findViewById(startButton + i).setBackgroundResource(R.color.tvRed);
        }
        posCounters=0;
        if(declined!=0) {
            for (int i = 0; i < pairsState.size(); i++)
                for (int j = 0; j < size; j++)
                    if (((pairsState.get(i)[j] > 2) && (pairsState.get(i)[j] < 6)) || (pairsState.get(i)[j] == 1))
                        posCounters++;
        }
        else posCounters=yMarksDone;
        ((TextView)findViewById(R.id.counter)).setText(Integer.toString(posCounters));
        if(posCounters == yMarks)((TextView)findViewById(R.id.counter)).setTextColor(Color.argb(255, 0, 0, 0));
        if(posCounters < yMarks)((TextView)findViewById(R.id.counter)).setTextColor(Color.argb(255,0,0,0));
        if(posCounters > yMarks)((TextView)findViewById(R.id.counter)).setTextColor(Color.argb(255, 180, 10, 10));
        findViewById(R.id.counter).setBackgroundResource((posCounters == yMarks) ? R.color.tvYes : R.color.material_grey_300);
        findViewById(R.id.nf_send).setEnabled(posCounters == yMarks);
        findViewById(R.id.nf_send).setBackgroundResource((posCounters == yMarks) ? R.color.tvYes : R.color.material_grey_300);
        for(int i=0;i<5;++i){
            if(Integer.valueOf((String)((Button) findViewById(R.id.button55 + i)).getText())==turnNumber+1)
                findViewById(R.id.button55 + i).setBackgroundResource(android.R.color.darker_gray);
            else
            if(Questioned(Integer.valueOf((String)((Button) findViewById(R.id.button55 + i)).getText())))
                findViewById(R.id.button55 + i).setBackgroundResource(R.color.tvMb);
            else
                findViewById(R.id.button55 + i).setBackgroundResource(android.R.color.background_light);
        }
    }

    private void CreateEventsNF(){
        for(int i=0;i<5;++i) {
            Button bck = (Button) findViewById(R.id.button55+i);
            bck.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    turnNumber = Integer.valueOf((String)(((Button)v).getText()))-1;
                    log("Turn setted to "+turnNumber);
                    WriteBackup();
                    SendInfo();
                    Send();
                    FillPairs();
                    SendLock(false);
                }
            });
        }
        Button bck = (Button) findViewById(R.id.nf_exit);
        bck.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                log("X pressed");
                Button bck1 = (Button) findViewById(R.id.nf_y);
                bck1.setVisibility(View.VISIBLE);
                findViewById(R.id.nf_n).setVisibility(View.VISIBLE);
                findViewById(R.id.nf_tb).setVisibility(View.GONE);
                bck1.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        log("ok pressed");
                        state = 0;
                        setContentView(R.layout.activity_main);
                        Send();
                        WriteBackup();
                        c.setDeletefiles("/airdance/" + String.valueOf(nomination_num) + "/judges", ((judge_num <= 9) ? "0" : "") + Integer.toString(judge_num) + ".lock");
                        c.setState(6);
                        SynWait(1);
                        recurseMain();
                    }
                });
            }
        });
        bck = (Button) findViewById(R.id.nf_n);
        bck.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                log("cancel pressed");
                (findViewById(R.id.nf_y)).setVisibility(View.INVISIBLE);
                (findViewById(R.id.nf_n)).setVisibility(View.INVISIBLE);
                (findViewById(R.id.nf_tb)).setVisibility(View.VISIBLE);
                findViewById(R.id.nf_send).setEnabled(posCounters == yMarks);
            }
        });
        bck = (Button) findViewById(R.id.button60);
        bck.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                tek = tek + 5;
                //if(tek>end)tek=end;
                turnNumber = tek - 1;
                log("Turn setted to "+turnNumber);
                for (int i = 0; i < 5; ++i) {
                    ((Button) findViewById(R.id.button55 + i)).setText(Integer.toString(tek + i));
                    if (tek + i > turnCount)
                        findViewById(R.id.button55 + i).setVisibility(View.INVISIBLE);
                    else findViewById(R.id.button55 + i).setVisibility(View.VISIBLE);
                }
                FillPairs();
            }
        });
        bck = (Button) findViewById(R.id.button54);
        bck.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                tek = tek - 5;
                turnNumber = tek + 3;
                log("Turn setted to " + turnNumber);
                for (int i = 0; i < 5; ++i) {
                    ((Button) findViewById(R.id.button55 + i)).setText(Integer.toString(tek + i));
                    if (tek + i > turnCount)
                        findViewById(R.id.button55 + i).setVisibility(View.INVISIBLE);
                    else findViewById(R.id.button55 + i).setVisibility(View.VISIBLE);
                }
                FillPairs();
            }
        });
        for(int i=0;i<size;++i){
            findViewById(startButton+i).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    log("Button clicked");
                    int n = v.getId() - startButton;
                    if (pairsState.get(turnNumber)[n] == 8) {declined--;red.remove(pairsNum.get(turnNumber)[n+1]);log("Declined "+n+" removed");}
                    pairsState.get(turnNumber)[n] = (pairsState.get(turnNumber)[n] + 1) % 3;
                    if (pairsState.get(turnNumber)[n] == 1) {yMarksDone++;log("Ymark "+n+" added");}
                    if (pairsState.get(turnNumber)[n] == 2) {yMarksDone--;log("Ymark "+n+" removed");}
                    FillPairs();
                    if(turnCount==1)Send();
                    //WriteBackup();
                }
            });
            findViewById(startButton+ i).setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    log("Button longclicked");
                    int n = v.getId() - startButton;
                    if (pairsState.get(turnNumber)[n] != 8) {
                        if (pairsState.get(turnNumber)[n] == 1) {
                            yMarksDone--;
                            log("Ymark " + n + " removed");
                        }
                        pairsState.get(turnNumber)[n] = 8;
                        declined++;
                        log("Declined " + n + " added");
                        red.add(pairsNum.get(turnNumber)[n + 1]);
                    }
                    FillPairs();
                    WriteBackup();
                    return true;
                }
            });
        }
        bck = (Button) findViewById(R.id.nf_send);
        bck.setOnClickListener(null);
        bck.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                log("Send clicked on " + danceNumber + "/" + danceCount + " dance");
                v.setEnabled(false);
                Button bck1 = (Button) findViewById(R.id.nf_y);
                bck1.setVisibility(View.VISIBLE);
                findViewById(R.id.nf_n).setVisibility(View.VISIBLE);
                findViewById(R.id.nf_tb).setVisibility(View.GONE);
                bck1.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        log("Ok pressed");
                        v.setVisibility(View.INVISIBLE);
                        findViewById(R.id.nf_n).setVisibility(View.INVISIBLE);
                        (findViewById(R.id.nf_tb)).setVisibility(View.VISIBLE);
                        if (posCounters != yMarks) return;
                        if (isSha) addsha();
                        Send();
                        if (danceNumber != danceCount) {
                            danceNumber++;
                            log("Next dance is " + danceNumber);
                            int n = ReadDance();
                            FillTitles(n);
                            FillPairs();
                            WriteBackup();
                            SendInfo();
                        } else {
                            c.setDeletefiles("/airdance/" + String.valueOf(nomination_num) + "/judges", ((judge_num <= 9) ? "0" : "") + Integer.toString(judge_num) + ".lock");
                            c.setState(6);
                            SynWait(1);
                            if (isSha) mainsha();
                            SendLock(true);
                            if (isSha) {
                                setContentView(R.layout.sha1);
                                ((TextView) findViewById(R.id.sha1)).setText(finsha);
                                Button bck = (Button) findViewById(R.id.returnmain);
                                bck.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        state = 0;
                                        setContentView(R.layout.activity_main);
                                        recurseMain();
                                    }
                                });
                            } else {
                                state = 0;
                                setContentView(R.layout.activity_main);
                                recurseMain();
                            }
                        }
                    }
                });
            }
        });
        bck = (Button) findViewById(R.id.nf_add);
        bck.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setContentView(R.layout.keyboard);
                t_key = "";
                ((EditText) findViewById(R.id.txt)).setText(t_key);
                mask = 3;
                auto = false;
                CreateKeyBoard();
                Button bck = (Button) findViewById(R.id.bo);
                bck.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        boolean q = setPair(t_key);
                        if (!q) {
                            addPairs.add(t_key);
                            yMarksDone++;
                        }
                        setContentView(nfLayout);
                        CreateEventsNF();
                        FillTitles(tc);
                        FillPairs();
                    }
                });
                bck = (Button) findViewById(R.id.key_back);
                bck.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        setContentView(nfLayout);
                        CreateEventsNF();
                        FillTitles(tc);
                        FillPairs();
                    }
                });
            }
        });
    }

    private void WriteBackup(){
        try {
            bWriter=new BufferedWriter(new FileWriter(backup));
            bWriter.write(t_nomination + '\n');
            bWriter.append(t_judge).append('\n');
            bWriter.append(Integer.toString(danceNumber)).append('\n');
            bWriter.append(Integer.toString(turnNumber)).append('\n');
            for(int i=0;i<pairsState.size();++i)
                for(int j=0;j<25;++j)
                    bWriter.append((j < pairsState.get(i).length) ? Integer.toString(pairsState.get(i)[j]) : "0").append('\n');
            bWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void Send(){
        try{
            String name="t"+((round<=9)?"0":"")+Integer.toString(round)+"j"+((judge_num<=9)?"0":"")+Integer.toString(judge_num)+"_"+pairsNum.get(0)[0]+".txt";
            File f=new File(nomPath+"/results",name);
            BufferedWriter bw=new BufferedWriter(new FileWriter(f));
            for(int i=0;i<pairsState.size();++i)
                for(int j=0;j<25;++j)
                    if((pairsState.get(i)[j]==1)||((pairsState.get(i)[j]>2)&&(pairsState.get(i)[j]<6))) {
                        bw.write(pairsNum.get(i)[j + 1]);
                        bw.write(13);
                        bw.write(10);
                    }
            for(int j=0;j<addPairs.size();++j){
                bw.write(addPairs.get(j));
                bw.write(13);
                bw.write(10);}
            bw.close();
            if(!lostconncetion){
                c.setFile(f);
                c.setUFile(name);
                c.setUPath("/airdance/" + String.valueOf(nomination_num) + "/results");
                c.setState(3);
                lostconncetion=SynWait(2);
            }
            if(lostconncetion)c.addreupload(f,"/airdance/" + String.valueOf(nomination_num) + "/results/"+name);
        }catch (Exception e){
            log("Exception in Send:"+e.getMessage());
            e.printStackTrace();}
    }

    private void SendLock(boolean last){
        try{
            String name,path;
            if(last){
                path="/airdance/" + String.valueOf(nomination_num) + "/results";
                name="t"+((round<9)?"0":"")+Integer.toString(round)+"j"+((judge_num<=9)?"0":"")+Integer.toString(judge_num) +".lock";}
            else{
                path="/airdance/" + String.valueOf(nomination_num) + "/judges";
                name=((judge_num<=9)?"0":"")+Integer.toString(judge_num) +".lock";}
            File f=new File(nomPath,name);
            BufferedWriter bw=new BufferedWriter(new FileWriter(f));
            if(last)bw.write("tour lock\n"+finsha);
            bw.close();
            if(!lostconncetion) {
                c.setFile(f);
                c.setUFile(name);
                c.setUPath(path);
                c.setState(3);
                lostconncetion = SynWait(2);
                CreateLocker(path,name);
            }
            if(lostconncetion)c.addreupload(f,"/airdance/" + String.valueOf(nomination_num) + "/results/"+name);
        }catch (Exception e){
            e.printStackTrace();}
    }

    private void SendInfo(){
        try{
            String name=Integer.toString(round)+"_"+Integer.toString(judge_num) +".txt";
            File f=new File(nomPath,name);
            BufferedWriter bw=new BufferedWriter(new FileWriter(f));
            bw.write(Integer.toString(danceNumber) + ":" + Integer.toString(turnNumber + 1));
            bw.close();
            c.setFile(f);
            c.setUFile(name);
            c.setUPath("/airdance/" + String.valueOf(nomination_num) + "/judges");
            c.setState(3);
            SynWait(1);
            if(lostconncetion)c.addreupload(f,"/airdance/" + String.valueOf(nomination_num) + "/judges/"+name);
            name=((judge_num<=9) ? "0" : "") + Integer.toString(judge_num) + ".stat";
            f=new File(sdPath,name);
            bw=new BufferedWriter(new FileWriter(f));
            bw.write(Integer.toString(battery_lvl));
            bw.close();
            if(!lostconncetion) {
                c.setFile(f);
                c.setUFile(name);
                c.setState(3);
                SynWait(1);
            }
            if(lostconncetion)c.addreupload(f,"/airdance/" + String.valueOf(nomination_num) + "/judges/"+name);
        }catch (Exception e){
            e.printStackTrace();}
    }

    private void SendF(){
        try{
            //((Button)findViewById(R.id.f_send)).setText("Working...");
            String name="t01j"+((judge_num<=9)?"0":"")+Integer.toString(judge_num)+"_"+pairsNum.get(0)[0].substring(1,pairsNum.get(0)[0].length()-1)+".txt";
            File f=new File(nomPath+"/results",name);
            BufferedWriter bw=new BufferedWriter(new FileWriter(f));
            for(int i=0;i<finAmount;++i)
                bw.write(pairsNum.get(0)[marksDone[i]]+"\r\n");
            bw.close();
            if(!lostconncetion) {
                c.setFile(f);
                c.setUFile(name);
                c.setUPath("/airdance/" + String.valueOf(nomination_num) + "/results");
                c.setState(3);
                lostconncetion = SynWait(2);
            }
            if(lostconncetion)c.addreupload(f,"/airdance/" + String.valueOf(nomination_num) + "/results/"+name);
        }catch (Exception e){
            log("Exception in SendF:"+e.getMessage());
            e.printStackTrace();}
    }

    private void WriteBackupF(){
        try {
            bWriter=new BufferedWriter(new FileWriter(backup));
            bWriter.write(t_nomination + '\n');
            bWriter.append(t_judge).append('\n');
            bWriter.append(Integer.toString(danceNumber)).append('\n');
            bWriter.append(Integer.toString(turnNumber)).append('\n');
            for(int i=0;i<pairsState.size();++i)
                for(int j=0;j<9;++j)
                    bWriter.append((j < pairsState.get(i).length) ? Integer.toString(pairsState.get(i)[j]) : "0").append('\n');
            bWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void ReadDanceF() {
        Integer[] x=new Integer[9];
        for(int i=0;i< 9;++i) {
            x[i] = 0;
            marksDone[i]=0;
        }
        pairsState.clear();
        pairsState.add(x);
        pairsNum.clear();
        yMarksDone=0;
        File sdFile = new File(sdPath, "tdance"+Integer.toString(danceNumber)+".txt");
        try{
            BufferedReader br =new BufferedReader(new FileReader(sdFile));
            String str = br.readLine();
            String[] tmp = str.split(";");
            finAmount=-1;
            while((finAmount+1<tmp.length)&&(!tmp[finAmount+1].equals("0")))
                finAmount++;
            if(finAmount>9)finAmount=9;
            if(tmp.length<10) {
                String[] newtmp = new String[10];
                int i=0;
                for(;i<tmp.length;++i)newtmp[i]=tmp[i];
                for(;i<10;++i)newtmp[i]="0";
                pairsNum.add(newtmp);
            }
            else
                pairsNum.add(tmp);
            if(restore){
                try{
                    restore=false;
                    BufferedReader rs = new BufferedReader(new FileReader(backup));
                    rs.readLine();rs.readLine();rs.readLine();
                    turnNumber=Integer.valueOf(rs.readLine());
                    for(int j=0;j<9;++j) {
                        int t = Integer.valueOf(rs.readLine());
                        if(t!=0)yMarksDone++;
                        pairsState.get(0)[j] = t;
                        if(t!=0)marksDone[t-1]=j+1;
                    }
                    br.close();
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
            else
                turnNumber=0;
            ((TextView)findViewById(R.id.finfo)).setText(t_nomination);
            ((TextView)findViewById(R.id.ffam)).setText(String.format("%s. %s", Integer.toString(judge_num), t_judge));
            ((TextView)findViewById(R.id.fdance)).setText((pairsNum.get(turnNumber)[0]).subSequence(1, pairsNum.get(turnNumber)[0].length() - 1));
            findViewById(R.id.textView26).setVisibility((pairsNum.get(0)[1].equals("0")) ? View.INVISIBLE : View.VISIBLE);
            findViewById(R.id.textView27).setVisibility((pairsNum.get(0)[2].equals("0")) ? View.INVISIBLE : View.VISIBLE);
            findViewById(R.id.textView28).setVisibility((pairsNum.get(0)[3].equals("0")) ? View.INVISIBLE : View.VISIBLE);
            findViewById(R.id.textView29).setVisibility((pairsNum.get(0)[4].equals("0")) ? View.INVISIBLE : View.VISIBLE);
            findViewById(R.id.textView31).setVisibility((pairsNum.get(0)[5].equals("0")) ? View.INVISIBLE : View.VISIBLE);
            findViewById(R.id.textView32).setVisibility((pairsNum.get(0)[6].equals("0")) ? View.INVISIBLE : View.VISIBLE);
            findViewById(R.id.textView33).setVisibility((pairsNum.get(0)[7].equals("0")) ? View.INVISIBLE : View.VISIBLE);
            findViewById(R.id.textView34).setVisibility((pairsNum.get(0)[8].equals("0")) ? View.INVISIBLE : View.VISIBLE);
            findViewById(R.id.textView35).setVisibility((pairsNum.get(0)[9].equals("0")) ? View.INVISIBLE : View.VISIBLE);
            ((TextView)findViewById(R.id.textView26)).setText(pairsNum.get(0)[1]);
            ((TextView)findViewById(R.id.textView27)).setText(pairsNum.get(0)[2]);
            ((TextView)findViewById(R.id.textView28)).setText(pairsNum.get(0)[3]);
            ((TextView)findViewById(R.id.textView29)).setText(pairsNum.get(0)[4]);
            ((TextView)findViewById(R.id.textView31)).setText(pairsNum.get(0)[5]);
            ((TextView)findViewById(R.id.textView32)).setText(pairsNum.get(0)[6]);
            ((TextView)findViewById(R.id.textView33)).setText(pairsNum.get(0)[7]);
            ((TextView)findViewById(R.id.textView34)).setText(pairsNum.get(0)[8]);
            ((TextView)findViewById(R.id.textView35)).setText(pairsNum.get(0)[9]);
            int mxheight=((720/finAmount)>80)?80:(720/finAmount);
            for(int i=0;i<9;++i){
                LinearLayout.LayoutParams lp=(LinearLayout.LayoutParams)findViewById(flines[i]).getLayoutParams();
                lp.height=(i<finAmount)?(mxheight):0;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void CreateEventsF(){
        (findViewById(R.id.f_y)).setVisibility(View.GONE);
        (findViewById(R.id.f_n)).setVisibility(View.GONE);
        (findViewById(R.id.f_ag)).setVisibility(View.GONE);
        Button[][] mrks=new Button[9][9];
        for(int i=0;i<finAmount;++i)
            for(int j=0;j<finAmount;++j) {
                mrks[i][j] = (Button) findViewById(fbutton[i] + j);
                findViewById(fbutton[i] + j).setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        boolean q=true;
                        for (int i = 0; (i < finAmount)&&q; ++i)
                            for (int j = 0; (j < finAmount)&&q; ++j)
                                if (v == findViewById(fbutton[i] + j)) {
                                    q=false;
                                    if (pairsState.get(0)[i] == 0) {
                                        pairsState.get(0)[i] = j + 1;
                                        marksDone[j] = i+1;
                                        yMarksDone++;
                                    } else {
                                        pairsState.get(0)[i] = 0;
                                        marksDone[j] = 0;
                                        yMarksDone--;
                                    }
                                }
                        FillFinal();
                        Send();
                        //WriteBackupF();
                    }
                });
            }
        Button bck = (Button) findViewById(R.id.f_ex);
        bck.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Button bck1 = (Button) findViewById(R.id.f_y);
                bck1.setVisibility(View.VISIBLE);
                findViewById(R.id.f_n).setVisibility(View.VISIBLE);
                findViewById(R.id.f_ag).setVisibility(View.VISIBLE);
                for (int i = 0; i < flines.length; ++i)
                    findViewById(flines[i]).setVisibility(View.GONE);
                bck1.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        v.setEnabled(false);
                        WriteBackupF();
                        SendF();
                        c.setDeletefiles("/airdance/" + String.valueOf(nomination_num) + "/judges", ((judge_num <= 9) ? "0" : "") + Integer.toString(judge_num) + ".lock");
                        c.setState(6);
                        SynWait(1);
                        state = 0;
                        setContentView(R.layout.activity_main);
                        recurseMain();
                    }
                });
            }
        });
        bck = (Button) findViewById(R.id.f_n);
        bck.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                (findViewById(R.id.f_y)).setVisibility(View.GONE);
                (findViewById(R.id.f_n)).setVisibility(View.GONE);
                (findViewById(R.id.f_ag)).setVisibility(View.GONE);
                for (int i = 0; i < flines.length; ++i)
                    findViewById(flines[i]).setVisibility(View.VISIBLE);
                findViewById(R.id.f_send).setEnabled(yMarksDone == finAmount);
            }
        });
        bck = (Button) findViewById(R.id.f_send);
        bck.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                v.setEnabled(false);
                Button bck1 = (Button) findViewById(R.id.f_y);
                bck1.setVisibility(View.VISIBLE);
                findViewById(R.id.f_n).setVisibility(View.VISIBLE);
                findViewById(R.id.f_ag).setVisibility(View.VISIBLE);
                for (int i = 0; i < flines.length; ++i)
                    findViewById(flines[i]).setVisibility(View.GONE);
                bck1.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        (findViewById(R.id.f_y)).setVisibility(View.GONE);
                        (findViewById(R.id.f_n)).setVisibility(View.GONE);
                        (findViewById(R.id.f_ag)).setVisibility(View.GONE);
                        for (int fline : flines) findViewById(fline).setVisibility(View.VISIBLE);
                        if (yMarksDone != finAmount) return;
                        SendF();
                        if (isSha) addshaF();
                        if (danceNumber != danceCount) {
                            danceNumber++;
                            ReadDanceF();
                            FillFinal();
                            WriteBackupF();
                            SendInfo();
                        } else {
                            if (isSha) mainsha();
                            SendLock(true);
                            c.setDeletefiles("/airdance/" + String.valueOf(nomination_num) + "/judges", ((judge_num <= 9) ? "0" : "") + Integer.toString(judge_num) + ".lock");
                            c.setState(6);
                            SynWait(1);
                            if (isSha) {
                                setContentView(R.layout.sha1);
                                ((TextView) findViewById(R.id.sha1)).setText(finsha);
                                Button bck = (Button) findViewById(R.id.returnmain);
                                bck.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        state = 0;
                                        setContentView(R.layout.activity_main);
                                        recurseMain();
                                    }
                                });
                            } else {
                                state = 0;
                                setContentView(R.layout.activity_main);
                                recurseMain();
                            }
                        }
                    }
                });
            }
        });
        FillFinal();
    }

    private void FillFinal(){
        //((Button)findViewById(R.id.f_send)).setText("Send");
        for(int i=0;i<9;++i){
            for(int j=0;j<9;++j) {
                if ((i >= finAmount) || (j >= finAmount)){
                    ((Button) findViewById(fbutton[i] + j)).setWidth(0);
                    continue;}
                if(pairsState.get(0)[i]==0)
                    if(marksDone[j]==0) {
                        ((Button) findViewById(fbutton[i] + j)).setWidth(470 / (finAmount - yMarksDone));
                        findViewById(fbutton[i] + j).setBackgroundResource(android.support.v7.appcompat.R.color.material_grey_100);
                        LinearLayout.LayoutParams lp=(LinearLayout.LayoutParams)findViewById(fbutton[i] + j).getLayoutParams();
                        lp.leftMargin=1;lp.rightMargin=1;
                    }
                    else {
                        ((Button) findViewById(fbutton[i] + j)).setWidth(0);
                        LinearLayout.LayoutParams lp=(LinearLayout.LayoutParams)findViewById(fbutton[i] + j).getLayoutParams();
                        lp.leftMargin=0;lp.rightMargin=0;
                    }
                else
                if(pairsState.get(0)[i]==(j+1)){
                    ((Button) findViewById(fbutton[i] + j)).setWidth(470);
                    findViewById(fbutton[i] + j).setBackgroundResource(R.color.tvYes);
                    LinearLayout.LayoutParams lp=(LinearLayout.LayoutParams)findViewById(fbutton[i] + j).getLayoutParams();
                    lp.leftMargin=1;lp.rightMargin=1;
                }
                else {
                    ((Button) findViewById(fbutton[i] + j)).setWidth(0);
                    LinearLayout.LayoutParams lp=(LinearLayout.LayoutParams)findViewById(fbutton[i] + j).getLayoutParams();
                    lp.leftMargin=0;lp.rightMargin=0;
                }
            }
        }
        ((TextView)findViewById(R.id.fcount)).setText(Integer.toString(yMarksDone));
        findViewById(R.id.f_send).setEnabled(yMarksDone == finAmount);
        findViewById(R.id.f_send).setBackgroundResource((yMarksDone == finAmount) ? R.color.tvYes : R.color.material_grey_300);
        String logtest="";
        for(int ij=0;ij<finAmount;++ij)
            logtest+=Integer.toString(pairsState.get(0)[ij])+";";

    }

    private boolean isOnline() {
        try {
            String cs = Context.CONNECTIVITY_SERVICE;
            ConnectivityManager cm = (ConnectivityManager)
                    getSystemService(cs);
            return cm.getActiveNetworkInfo() != null;
        }catch(Exception e){log(e.getMessage());
            return true;}
    }

    private boolean SynWait(float sec){
        int count=(int)(sec*50);
        try{
            while(!c.Done()&&(count!=0)) {
                Thread.sleep(20);
                count--;
            }
        }catch(InterruptedException e){log("Exception in SynWait:"+e.getMessage());}
        return count==0;
    }

    static String sha1(String input) throws NoSuchAlgorithmException {
        MainActivity.input = input;
        MessageDigest mDigest = MessageDigest.getInstance("SHA1");
        byte[] result = mDigest.digest(input.getBytes());
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < result.length; i++) {
            sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1));
        }

        return sb.toString();
    }

    public void addsha(){
        String start= "";
        ArrayList<Integer> x=new ArrayList<>();
        for(int i=0;i<pairsState.size();++i)
            for(int j=0;j<25;++j)
                if(pairsState.get(i)[j]==1) {
                    x.add(Integer.valueOf(pairsNum.get(i)[j+1]));
                }
        Collections.sort(x);
        for(int i=0;i<x.size();++i)start+=Integer.toString(x.get(i));
        try {
            sha.add(sha1(start));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public void addshaF(){
        String start= "";
        for(int i=0;i<finAmount;++i)
            start+=pairsNum.get(0)[marksDone[i]];
        try {
            sha.add(sha1(start));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public void mainsha(){
        int min=5;
        int max=5;
        int val=0;
        int s;
        while ((gruppa[max].length()>0)&&(max<gruppa.length))
            max++;
        shas=new byte[20*(max-min)];
        for(int t=0;t<max-min;++t) {
            s=-1;
            for (int i = min; i < max; ++i)
                if ((gruppa[i].equalsIgnoreCase(dord.get(val)))){s=i-min; break;}
            if(s>=sha.size())continue;
            if(s<0)continue;
            for(int j=0;j<20;++j)
                addbytes(sha.get(s).charAt(2*j),sha.get(s).charAt(2*j+1),20*t+j);
            val++;
        }
        try {
            MessageDigest mDigest = MessageDigest.getInstance("SHA1");
            byte[] result = mDigest.digest(shas);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < result.length; i++)
                sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1));
            String str=sb.toString().substring(sb.length()-4);
            finsha=Integer.toString(Integer.parseInt(str, 16) % 10000);
        }catch (Exception e){log("Exception in mainsha:" + e.getMessage());}
        sha.clear();
    }

    public void addbytes(char tmp1,char tmp2,int s){
        int x=0,y=0;
        switch(tmp1){
            case '0':{x=0;break;}
            case '1':{x=1;break;}
            case '2':{x=2;break;}
            case '3':{x=3;break;}
            case '4':{x=4;break;}
            case '5':{x=5;break;}
            case '6':{x=6;break;}
            case '7':{x=7;break;}
            case '8':{x=8;break;}
            case '9':{x=9;break;}
            case 'a':{x=10;break;}
            case 'b':{x=11;break;}
            case 'c':{x=12;break;}
            case 'd':{x=13;break;}
            case 'e':{x=14;break;}
            case 'f':{x=15;break;}
        }
        switch(tmp2){
            case '0':{y=16*x;break;}
            case '1':{y=16*x+1;break;}
            case '2':{y=16*x+2;break;}
            case '3':{y=16*x+3;break;}
            case '4':{y=16*x+4;break;}
            case '5':{y=16*x+5;break;}
            case '6':{y=16*x+6;break;}
            case '7':{y=16*x+7;break;}
            case '8':{y=16*x+8;break;}
            case '9':{y=16*x+9;break;}
            case 'a':{y=16*x+10;break;}
            case 'b':{y=16*x+11;break;}
            case 'c':{y=16*x+12;break;}
            case 'd':{y=16*x+13;break;}
            case 'e':{y=16*x+14;break;}
            case 'f':{y=16*x+15;break;}
        }
        shas[s]=(byte)y;
    }

    public void CreateKeyBoard(){
        Button bck = (Button) findViewById(R.id.b0);
        bck.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(t_key.length()<mask)
                t_key+="0";
                ((EditText)findViewById(R.id.txt)).setText(t_key);
                if(auto&&(t_key.length()==mask))findViewById(R.id.bo).callOnClick();
            }
        });
        bck = (Button) findViewById(R.id.b1);
        bck.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(t_key.length()<mask)
                t_key+="1";
                ((EditText)findViewById(R.id.txt)).setText(t_key);
                if(auto&&(t_key.length()==mask))findViewById(R.id.bo).callOnClick();
            }
        });
        bck = (Button) findViewById(R.id.b2);
        bck.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(t_key.length()<mask)
                t_key+="2";
                ((EditText)findViewById(R.id.txt)).setText(t_key);
                if(auto&&(t_key.length()==mask))findViewById(R.id.bo).callOnClick();
            }
        });
        bck = (Button) findViewById(R.id.b3);
        bck.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(t_key.length()<mask)
                t_key+="3";
                ((EditText)findViewById(R.id.txt)).setText(t_key);
                if(auto&&(t_key.length()==mask))findViewById(R.id.bo).callOnClick();
            }
        });
        bck = (Button) findViewById(R.id.b4);
        bck.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(t_key.length()<mask)
                t_key+="4";
                ((EditText)findViewById(R.id.txt)).setText(t_key);
                if(auto&&(t_key.length()==mask))findViewById(R.id.bo).callOnClick();
            }
        });
        bck = (Button) findViewById(R.id.b5);
        bck.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(t_key.length()<mask)
                t_key+="5";
                ((EditText)findViewById(R.id.txt)).setText(t_key);
                if(auto&&(t_key.length()==mask))findViewById(R.id.bo).callOnClick();
            }
        });
        bck = (Button) findViewById(R.id.b6);
        bck.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(t_key.length()<mask)
                t_key+="6";
                ((EditText)findViewById(R.id.txt)).setText(t_key);
                if(auto&&(t_key.length()==mask))findViewById(R.id.bo).callOnClick();
            }
        });
        bck = (Button) findViewById(R.id.b7);
        bck.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(t_key.length()<mask)
                t_key+="7";
                ((EditText)findViewById(R.id.txt)).setText(t_key);
                if(auto&&(t_key.length()==mask))findViewById(R.id.bo).callOnClick();
            }
        });
        bck = (Button) findViewById(R.id.b8);
        bck.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(t_key.length()<mask)
                t_key+="8";
                ((EditText)findViewById(R.id.txt)).setText(t_key);
                if(auto&&(t_key.length()==mask))findViewById(R.id.bo).callOnClick();
            }
        });
        bck = (Button) findViewById(R.id.b9);
        bck.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(t_key.length()<mask)
                t_key+="9";
                ((EditText)findViewById(R.id.txt)).setText(t_key);
                if(auto&&(t_key.length()==mask))findViewById(R.id.bo).callOnClick();
            }
        });
        bck = (Button) findViewById(R.id.bc);
        bck.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                t_key="";
                ((EditText)findViewById(R.id.txt)).setText(t_key);
            }
        });
    }

    protected int pow(int x){
        int r=1;
        for(int i=1;i<x;++i)r*=2;
        return r;
    }

    public void log(String s){
            try {
                if(debug) {
                    BufferedWriter b = new BufferedWriter(new FileWriter(logger2, true));
                    b.append(Calendar.getInstance().getTime() + "$");
                    b.append(s);
                    b.newLine();
                    b.close();
                }
            }catch (Exception e){e.printStackTrace();}
    }

    public void CreateLangListners(){
        ImageButton bck = (ImageButton) findViewById(R.id.eng);
        bck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lang=false;
                Locale lc=new Locale("en");
                Configuration cf=new Configuration();
                cf.locale=lc;
                getBaseContext().getResources().updateConfiguration(cf,null);
                setContentView(R.layout.activity_main);
                CreteMainListeners();
            }
        });
        bck = (ImageButton) findViewById(R.id.rus);
        bck.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View v) {
                lang=true;
                Locale lc=new Locale("ru");
                Configuration cf=new Configuration();
                cf.locale=lc;
                getBaseContext().getResources().updateConfiguration(cf,null);
                setContentView(R.layout.activity_main);
                CreteMainListeners();
            }
        });
    }

    public void SetRed(){
        for(int i=0;i<pairsNum.size();++i)
            for(int j=1;j<pairsNum.get(i).length;++j)
                for(int k=0;k<red.size();++k)
                if(pairsNum.get(i)[j].equals(red.get(k))) {
                    pairsState.get(i)[j - 1] = 8;
                    declined++;
                }
    }

    public void Rescue(){
        for(int nom=1;nom<100;++nom) {
            resPath=new File(path+"/"+nom+"/results");
            if (resPath.exists()&&resPath.isDirectory()) {
                for (File f : resPath.listFiles()) {
                    c.setFile(f);
                    c.setUFile(f.getName());
                    c.setUPath("/rescue/" + String.valueOf(nom) + "/results");
                    c.setState(3);
                    SynWait(1);
                }
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        return keyCode== KeyEvent.KEYCODE_MENU||keyCode==KeyEvent.KEYCODE_BACK||keyCode==KeyEvent.KEYCODE_HOME||super.onKeyDown(keyCode,event);
    }

    public void DeleteTmp(){
        for(File file : sdPath.listFiles()) {
            if(!(file.getName().equals("cfg.txt")||file.getName().equals("settings.txt")||file.getName().equals(".android_secure")||file.getName().equals("log.txt")||file.getName().equals("alllog.txt")||file.getName().equals("settings.txt")
                    ||file.isDirectory()))
                if(!file.delete())log("Deleting "+file.getName()+" failed");
        }
    }

    public void DeleteAll(){
        DeleteTmp();
        for(int nom=1;nom<100;++nom) {
            resPath = new File(path + "/" + nom + "/results");
            if (resPath.exists() && resPath.isDirectory()) {
                for (File f : resPath.listFiles()) {
                    if(!f.delete())log("Deleting "+f.getName()+" failed");
                }
            }
        }
    }

    public void CreateSettingsListeners(){
        Button btn = (Button) findViewById(R.id.s_restore);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Rescue();
                LinearLayout ll=(LinearLayout)findViewById(R.id.s_layout);
                final int childcount = ll.getChildCount();
                for (int i = 0; i < childcount; i++)
                    ll.getChildAt(i).setVisibility(View.GONE);
                findViewById(R.id.s_ok).setVisibility(View.VISIBLE);
            }
            });
        btn = (Button) findViewById(R.id.s_clear);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeleteTmp();
            }
        });
        btn = (Button) findViewById(R.id.s_fullclear);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeleteAll();
                LinearLayout ll=(LinearLayout)findViewById(R.id.s_layout);
                final int childcount = ll.getChildCount();
                for (int i = 0; i < childcount; i++)
                    ll.getChildAt(i).setVisibility(View.GONE);
                findViewById(R.id.s_ok).setVisibility(View.VISIBLE);
            }
        });
        btn = (Button) findViewById(R.id.s_debug);
        btn.setText(String.format("%s %s", getResources().getText(R.string.Debug), getResources().getText(debug?R.string.On:R.string.Off)));
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                debug=!debug;
                ((Button) findViewById(R.id.s_debug)).setText(String.format("%s %s", getResources().getText(R.string.Debug), getResources().getText(debug?R.string.On:R.string.Off)));
                SaveSettings();
            }
        });
        btn = (Button) findViewById(R.id.s_tomain);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (newinit) Init();
                setContentView(R.layout.activity_main);
                CreteMainListeners();
            }
        });
        btn = (Button) findViewById(R.id.s_ok);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (newinit) Init();
                setContentView(R.layout.activity_main);
                CreteMainListeners();
            }
        });
    }

    public void CreateLocker(final String filepath,final String filename){
        (th=new Thread(new Runnable() {
            @Override
        public void run() {
                File f=new File(sdPath,filename);
                if(!f.exists())return;
                int count=0;
            while(!c.exists(filepath,filename)&&state>=1&&count<10)
                try {
                    c.setFile(f);
                    c.setUPath(filepath);
                    c.setUFile(filename);
                    c.setState(3);
                    count++;
                    Thread.sleep(5000);
                }catch (Exception e){}
            }
        })).start();
    }

    public void DisplayCharge(){
        (th=new Thread(new Runnable() {
            Runnable settext=new Runnable() {
                @Override
                public void run() {
                    TextView charge = (TextView) findViewById(R.id.charge);
                    if(charge!=null) {
                        charge.setText(battery_lvl + "%");
                        if(battery_lvl>60)charge.setBackgroundResource(R.color.tvYes);
                        else if(battery_lvl>30)charge.setBackgroundResource(R.color.tvMb);
                        else charge.setBackgroundResource(R.color.tvRed);
                    }
                }
            };
            @Override
            public void run() {
                while (true) {
                    try {
                        runOnUiThread(settext);
                        Thread.sleep(30000);
                        //c.setState(10);
                    } catch (Exception e) {}
                    try {
                        URL url = new URL(c.host);
                        URLConnection con = url.openConnection();
                        con.setConnectTimeout(2000);
                        con.setReadTimeout(2000);
                        con.connect();
                    } catch (Exception e) {}
                }
            }
        })).start();
    }

    public void ReadSettings(){
        File settings= new File(path,"settings.txt");
        if(settings.exists())
        try{
            BufferedReader br = new BufferedReader(new FileReader(settings));
            debug = Boolean.valueOf(br.readLine());
            br.close();
        }catch(Exception e){}
    }

    public void SaveSettings(){
        File settings= new File(path,"settings.txt");
            try{
                BufferedWriter br = new BufferedWriter(new FileWriter(settings));
                br.write(Boolean.toString(debug));
                br.newLine();
                br.close();
            }catch(Exception e){}
    }

}

class Connecter implements Runnable{
    private int state=0;
    private InetAddress addr;
    private FTPClient ftp;
    public String host,user,pass;
    private String dfile,f;
    private String dpath,pth;
    private String ufile;
    private String upath;
    private Stack<String> deletefiles=new Stack<>();
    private ArrayList<File> upfiles=new ArrayList<>();
    private ArrayList<String> unames=new ArrayList<>();
    private File locfile;
    private boolean q,cl,ex;
    private BufferedWriter logger;
    private MainActivity mac;
    public ArrayList<String> strs=new ArrayList<>();
    public boolean Done(){return cl;}
    public synchronized void setState(int st){state=st;(new Thread(this)).start();cl=false;}
    public void setFile(File f){locfile=f;}
    public void setLog(BufferedWriter f){logger=f;}
    public void setDFile(String f){dfile=f;}
    public void setDPath(String f){dpath=f;}
    public void setUFile(String f){ufile=f;}
    public void setUPath(String f){upath=f;}
    public void setCPath(String f){pth=f;}
    public void setGlobals(String h,String u, String p){host=h;user=u;pass=p;}
    public void setDeletefiles(String dlpath,String dlname){deletefiles.push(dlpath+"/"+dlname);}
    public boolean exists(String _pth,String _f)  {
        q=false;
        pth=_pth;
        f=_f;
        setState(5);
        ex=true;
        int count=10;
        try{while(ex&&(count!=0))
            {Thread.sleep(50);count--;}
        }
        catch (Exception e){mac.log("Exception when try to check file:"+e.getMessage());}
        return q;
    }
    public void startReconnect(MainActivity ma){
        new Thread(new Reconnect(this,20,ma)).start();
        mac.log("Recconect thread started");
    }
    public void addreupload(File f,String name){
        int n=-1;
        for(int i=0;i<unames.size();++i){
            if(unames.get(i).equals(name))
                n=i;
        }
        if(n!=-1)
            upfiles.set(n,f);
        else {
            upfiles.add(f);
            unames.add(name);
        }
    }
    public void init(MainActivity ma){
        mac=ma;
        try {
            addr = InetAddress.getByName(host);
        }catch (Exception e){mac.log("Init of connecter failed");}
    }

    public void Connect(){
        try {
            try {
                ftp.disconnect();
            }catch(Exception e){}
            ftp = new FTPClient();
            ftp.connect(addr);
            boolean sucs = ftp.login(user, pass);
            if (sucs) {
                ftp.enterLocalPassiveMode();
            } else {
                sucs=ftp.login("anonymous", "");
                if(sucs)
                    ftp.enterLocalPassiveMode();
            }
            if(sucs)run();
        }catch (Exception e){}
    }
    @Override
    public void run() {
        try {
        int tstate=state;
            while (tstate != 0) {
                switch (tstate) {
                    case 1: {
                        mac.log("Start new session:" + addr.getHostAddress());
                        mac.log("Init new FTP session");
                        ftp = new FTPClient();
                        ftp.connect(addr);
                        mac.log(ftp.getReplyString());
                        boolean sucs = ftp.login(user, pass);
                        mac.log("Init result is " + sucs);
                        mac.log(ftp.getReplyString());
                        if (sucs) {
                            ftp.enterLocalPassiveMode();
                            cl=true;
                            tstate = 0;
                            mac.log("Init sucssesful");
                        } else {
                            mac.log("Try anon");
                            boolean sucs2 = ftp.login("anonymous", "");
                            mac.log("Init result is " + sucs2);
                            mac.log(ftp.getReplyString());
                            cl=sucs2;
                            if (sucs2) {
                                tstate = 0;
                                mac.log("Init sucssesful");
                            }
                            else {
                                mac.log("No logon");
                            }
                        }
                        break;
                    }
                    case 2:
                        FTPFile[] files;
                    {
                        mac.log("Init download");
                        files = ftp.listFiles(dpath+'/'+dfile);
                        mac.log(ftp.getReplyString());
                        mac.log("With name "+dfile+" founded "+ files.length);
                        if (files.length != 0) {
                            FileOutputStream fos = new FileOutputStream(locfile);
                            ftp.retrieveFile(dpath+'/'+dfile, fos);
                            mac.log(files[0].getName());
                            mac.log("Download " + dfile+":"+ftp.getReplyString());
                            fos.close();
                        } else {
                            mac.log("No such file:" + dfile);
                        }
                        tstate = 0;
                        cl=true;
                        mac.log("Download from FTP ended");
                        break;
                    }
                    case 3: {
                        mac.log("Init upload");
                        FileInputStream fis = new FileInputStream(locfile);
                        try {
                            boolean sucsu=ftp.storeFile(upath + "/" + ufile, fis);
                            mac.log("Uploaded "+ufile+":"+ftp.getReplyString());
                            if(sucsu){
                            cl=true;
                            tstate=0;}
                        }catch (Exception e){
                            mac.log(e.getMessage());
                            mac.log("Lost connection, can't upload " + upath + "/" + ufile);
                            tstate = 0;
                        }
                        fis.close();
                        mac.log("Upload to FTP ended");
                        break;
                    }
                    case 4: {
                        ftp.logout();
                        mac.log(ftp.getReplyString());
                        ftp.disconnect();
                        mac.log(ftp.getReplyString() + '\n');
                        tstate = 0;
                        break;
                    }
                    case 5: {
                        mac.log("Check " + f);
                        files = ftp.listFiles(pth+"/"+f);
                        mac.log(ftp.getReplyString());
                        q = files.length != 0;
                        ex=false;
                        tstate = 0;
                        break;
                    }
                    case 6: {
                        for (String s : deletefiles) {
                            mac.log("Trying to delete " + s);
                            files = ftp.listFiles(s);
                            mac.log(ftp.getReplyString());
                            if (files.length != 0) {
                                boolean sucsf = ftp.deleteFile(s);
                                mac.log(ftp.getReplyString());
                                if (sucsf) deletefiles.remove(s);
                            } else deletefiles.remove(s);
                        }
                        if(deletefiles.empty()){tstate=0;cl=true;}
                        else Thread.sleep(5000);
                        mac.log("Deleting ends");
                        break;
                    }
                    case 7:{
                        mac.log("Reconnect...");
                        ftp = new FTPClient();
                        ftp.connect(addr);
                        mac.log(ftp.getReplyString());
                        boolean sucsr = ftp.login(user, pass);
                        mac.log(ftp.getReplyString());
                        if (sucsr) {
                            ftp.enterLocalPassiveMode();
                            mac.log(ftp.getReplyString());
                            cl=true;
                            tstate = 0;
                        } else {
                            mac.log("Try anon");
                            boolean sucs2r = ftp.login("anonymous", "");
                            mac.log(ftp.getReplyString());
                            cl=sucs2r;
                            if (sucs2r) {
                                ftp.enterLocalPassiveMode();
                                mac.log(ftp.getReplyString());
                                tstate = 0;
                            }
                            else
                                mac.log("No logon");
                        }
                        break;
                    }
                    case 8: {
                        mac.log("Start reuploading " + upfiles.size() + " files");
                        int errors=0;
                        ArrayList<File> newfiles=new ArrayList<>();
                        ArrayList<String> newnames=new ArrayList<>();
                        for(int i=0;i<upfiles.size();++i) {
                            if(upfiles.get(i)!=null&&upfiles.get(i).exists()) {
                                FileInputStream fis = new FileInputStream(upfiles.get(i));
                                try {
                                    ftp.storeFile(unames.get(i), fis);
                                    logger.write(ftp.getReplyString());
                                    if(ftp.listFiles(unames.get(i)).length==0){
                                        newnames.add(unames.get(i));
                                        newfiles.add(upfiles.get(i));
                                    }
                                    cl = true;
                                } catch (Exception e) {
                                    mac.log(e.getMessage());
                                    mac.log("Lost connection, can't upload " + upath + "/" + ufile);
                                    errors++;
                                }
                                fis.close();
                            }
                        }
                        tstate=0;
                        if(errors==0){upfiles.clear();unames.clear();upfiles=newfiles;unames=newnames;}
                        if(unames.size()>0) {
                            try {
                                Thread.sleep(5000);
                            }catch(Exception e){}
                            this.setState(8);
                        }
                        mac.log("Reuploading ends with " + errors + "errors");
                        break;
                    }
                    case 9: {
                        files = ftp.listFiles(pth);
                        mac.log(ftp.getReplyString());
                        for(int i=0;i< files.length;++i)
                            strs.add(files[i].getName());
                        mac.log("Exist lock " + files.length + " files");
                        tstate = 0;
                        cl=true;
                        break;
                    }
                    case 10:{
                        tstate=0;
                        break;
                    }
                }
                if(tstate!=0)
                    Thread.sleep(50);
            }
            if(state==10){
                FTPFile[] test = ftp.listFiles("/airdance/");
                SimpleDateFormat sf=new SimpleDateFormat("HH:mm:ss");
                mac.log(sf.format(Calendar.getInstance().getTime())+((test.length>0)?":connection ready":":connection lost"));
                if(test.length==0) {
                    ftp = new FTPClient();
                    ftp.connect(addr);
                    mac.log(ftp.getReplyString());
                    boolean r_sucs = ftp.login(user, pass);
                    mac.log("Init result is " + r_sucs);
                    mac.log(ftp.getReplyString());
                    if (r_sucs) {
                        ftp.enterLocalPassiveMode();
                        mac.log("Init sucssesful");
                    } else {
                        mac.log("Try anon");
                        boolean r_sucs2 = ftp.login("anonymous", "");
                        mac.log("Init result is " + r_sucs2);
                        mac.log(ftp.getReplyString());
                        if (r_sucs2) {
                            mac.log("Init sucssesful");
                        } else {
                            mac.log("No logon");
                        }
                    }
                }
            }
        } catch (Exception e) {mac.log("Exception in run of connecter:"+e.getMessage());Connect();}
    }

}

class Reconnect implements Runnable{
    private Connecter ftp;
    private int cooldown;
    private MainActivity dnce;
    Reconnect(Connecter f,int cd,MainActivity ma){ftp=f;cooldown=cd;dnce=ma;}
    @Override
    synchronized public void run() {
        Random rnd=new Random();
        while(ftp!=null) {
            try {
                if (dnce.lostconncetion) {
                    ftp.setState(7);
                    int count = 100;
                    try {
                        while (!ftp.Done() && (count != 0)) {
                            Thread.sleep(20);
                            count--;
                        }
                    } catch (InterruptedException e) {dnce.log("Run of reconnect failed");}
                    dnce.lostconncetion = (count == 0);
                    if(!dnce.lostconncetion) {
                        ftp.setState(8);
                        Thread.sleep(rnd.nextInt(300));
                    }
                }
                Thread.sleep(cooldown*1000);
            }catch(Exception e){dnce.log("Run of reconnect failed");}
        }
    }

}