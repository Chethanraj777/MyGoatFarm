package com.mygoatfarm.app

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import org.json.JSONObject
import java.text.NumberFormat
import java.util.*

data class FarmRecord(
    val date: String, val type: String, val goat: String, val details: String,
    val qty: Double, val qtyText: String, val amount: Double, val sex: String,
    val breed: String, val goatId: String, val notes: String
)

class MainActivity : AppCompatActivity() {
    private val prefs by lazy { getSharedPreferences("farm", Context.MODE_PRIVATE) }
    private val records = mutableListOf<FarmRecord>()
    private lateinit var content: LinearLayout
    private val inr = NumberFormat.getCurrencyInstance(Locale("en","IN"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        load()
        build()
        showDashboard()
    }

    private fun dp(n:Int)= (n*resources.displayMetrics.density).toInt()
    private fun tv(text:String,size:Float=16f,bold:Boolean=false):TextView =
        TextView(this).apply { this.text=text; textSize=size; setPadding(dp(10),dp(8),dp(10),dp(8)); if(bold) setTypeface(null,1) }

    private fun build() {
        val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL}
        val head=LinearLayout(this).apply{
            orientation=LinearLayout.VERTICAL; setPadding(dp(18),dp(18),dp(18),dp(12))
            setBackgroundColor(android.graphics.Color.rgb(22,101,52))
        }
        head.addView(tv("🐐  My Goat Farm",25f,true).apply{setTextColor(-1)})
        head.addView(tv("Goat management & farm accounts",14f).apply{setTextColor(0xffe8f5e9.toInt())})
        root.addView(head)

        val nav=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;setPadding(dp(6),dp(6),dp(6),dp(6))}
        listOf("Dashboard","Goats","Records","Add").forEachIndexed{ i,s->
            nav.addView(Button(this).apply{
                text=s; setOnClickListener{when(i){0->showDashboard();1->showGoats();2->showRecords();3->showAdd()}}
            },LinearLayout.LayoutParams(0,dp(52),1f))
        }
        root.addView(nav)
        val scroll=ScrollView(this)
        content=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(14),0,dp(14),dp(20))}
        scroll.addView(content); root.addView(scroll,LinearLayout.LayoutParams(-1,0,1f))
        setContentView(root)
    }

    private fun clear(){content.removeAllViews()}
    private fun money(x:Double)=inr.format(x)
    private fun totalSpent()=records.filter{it.type in listOf("purchase","feed","medicine","expense","infrastructure")}.sumOf{it.amount}
    private fun purchased()=records.filter{it.type=="purchase"}.sumOf{it.qty}.toInt()
    private fun births()=records.filter{it.type=="birth"}.sumOf{if(it.qty>0)it.qty else 1.0}.toInt()
    private fun sold()=records.filter{it.type=="sale"}.sumOf{if(it.qty>0)it.qty else 1.0}.toInt()

    private fun showDashboard(){
        clear()
        content.addView(tv("Farm Summary",22f,true))
        val herd=purchased()+births()-sold()
        val income=records.filter{it.type=="sale"}.sumOf{it.amount}
        listOf(
            "🐐 Current herd: $herd goats",
            "🍼 Kids born: ${births()}",
            "🐐 Goats purchased: ${purchased()}",
            "💸 Total recorded spending: ${money(totalSpent())}",
            "💰 Sales income: ${money(income)}",
            "📊 Net cash position: ${money(income-totalSpent())}"
        ).forEach{content.addView(tv(it,18f,true))}
        content.addView(tv("\nCurrent starting records\n• 26/07/2026: 4 goats — ₹50,000\n• 04/08/2026: 3 goats — ₹26,000\n• 07/08/2026: Nati medicine — ₹600\n• 31/08/2026: Shiva, male kid\n• 04/09/2026: female kid\n• 28/09/2026: groundnut cake 10 kg — ₹580\n• 28/09/2026: maize 10 kg — ₹340\n• Goat shed — ₹1,10,000",16f))
        val backup=Button(this).apply{text="Backup / Export";setOnClickListener{exportText()}}
        content.addView(backup)
    }

    private fun showGoats(){
        clear(); content.addView(tv("Goat Register",22f,true))
        val named=records.filter{it.goat.isNotBlank()||it.goatId.isNotBlank()}
        if(named.isEmpty()) content.addView(tv("No individual goat IDs/names yet.\nUse Add to enter each goat.",17f))
        named.forEach{content.addView(tv("${it.date}  •  ${it.goatId} ${it.goat}\n${it.sex} ${it.breed}\n${it.details}  ${money(it.amount)}",16f))}
    }

    private fun showRecords(){
        clear(); content.addView(tv("All Farm Records",22f,true))
        records.sortedByDescending{it.date}.forEach{r->
            content.addView(tv("${r.date}  •  ${r.type.uppercase()}\n${r.goat} ${r.details}\nQty: ${r.qtyText}   Amount: ${money(r.amount)}${if(r.notes.isNotBlank())"\n"+r.notes else ""}",16f))
            content.addView(View(this).apply{setBackgroundColor(0xffdddddd.toInt())},LinearLayout.LayoutParams(-1,1))
        }
    }

    private fun showAdd(){
        clear(); content.addView(tv("Add New Record",22f,true))
        val date=EditText(this).apply{hint="Date (YYYY-MM-DD)";setText(java.text.SimpleDateFormat("yyyy-MM-dd",Locale.US).format(Date()))}
        val type=Spinner(this); type.adapter=ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,
            arrayOf("purchase","birth","feed","medicine","sale","expense","infrastructure"))
        val goat=EditText(this).apply{hint="Goat / Kid name"}
        val details=EditText(this).apply{hint="Details"}
        val qty=EditText(this).apply{hint="Quantity (number, e.g. 1 or 10)";inputType=2}
        val qtyText=EditText(this).apply{hint="Quantity text (e.g. 10 kg)"}
        val amount=EditText(this).apply{hint="Amount ₹";inputType=8194}
        val sex=EditText(this).apply{hint="Sex (Male/Female)"}
        val breed=EditText(this).apply{hint="Breed"}
        val id=EditText(this).apply{hint="Goat ID (e.g. G-001)"}
        val notes=EditText(this).apply{hint="Notes"}
        listOf(date,goat,details,qty,qtyText,amount,sex,breed,id,notes).forEach{content.addView(it,LinearLayout.LayoutParams(-1,dp(58)))}
        content.addView(type)
        // Keep type near top as well via label; spinner is intentionally before save.
        val save=Button(this).apply{text="Save Record";setOnClickListener{
            records.add(FarmRecord(date.text.toString(),type.selectedItem.toString(),goat.text.toString(),details.text.toString(),
                qty.text.toString().toDoubleOrNull()?:1.0,qtyText.text.toString(),amount.text.toString().toDoubleOrNull()?:0.0,
                sex.text.toString(),breed.text.toString(),id.text.toString(),notes.text.toString()))
            persist(); Toast.makeText(this@MainActivity,"Record saved",Toast.LENGTH_SHORT).show(); showRecords()
        }}
        content.addView(save)
    }

    private fun persist(){
        val a=JSONArray()
        records.forEach{r->a.put(JSONObject().apply{
            put("date",r.date);put("type",r.type);put("goat",r.goat);put("details",r.details);put("qty",r.qty)
            put("qtyText",r.qtyText);put("amount",r.amount);put("sex",r.sex);put("breed",r.breed);put("goatId",r.goatId);put("notes",r.notes)
        })}
        prefs.edit().putString("records",a.toString()).apply()
    }
    private fun load(){
        val raw=prefs.getString("records",null)
        if(raw==null){
            records.addAll(listOf(
                FarmRecord("2026-07-26","purchase","", "4 goats purchased",4.0,"4 goats",50000.0,"","","",""),
                FarmRecord("2026-08-04","purchase","", "3 goats purchased",3.0,"3 goats",26000.0,"","","",""),
                FarmRecord("2026-08-07","medicine","", "Nati medicine",1.0,"1",600.0,"","","",""),
                FarmRecord("2026-08-31","birth","Shiva","Male kid born",1.0,"1",0.0,"Male","","",""),
                FarmRecord("2026-09-04","birth","","Female kid born",1.0,"1",0.0,"Female","","",""),
                FarmRecord("2026-09-28","feed","","Groundnut cake",10.0,"10 kg",580.0,"","","",""),
                FarmRecord("2026-09-28","feed","","Maize",10.0,"10 kg",340.0,"","","",""),
                FarmRecord("2026-09-28","infrastructure","","Goat farm shed",1.0,"1",110000.0,"","","","")
            ));persist()
        }else{
            val a=JSONArray(raw); for(i in 0 until a.length()){val o=a.getJSONObject(i);records.add(FarmRecord(
                o.getString("date"),o.getString("type"),o.optString("goat"),o.optString("details"),
                o.optDouble("qty",1.0),o.optString("qtyText"),o.optDouble("amount",0.0),o.optString("sex"),
                o.optString("breed"),o.optString("goatId"),o.optString("notes")
            ))}
        }
    }
    private fun exportText(){
        val sb=StringBuilder("My Goat Farm Backup\n\n")
        records.forEach{sb.append("${it.date} | ${it.type} | ${it.goat} | ${it.details} | ${it.qtyText} | ${it.amount}\n")}
        val i=android.content.Intent(android.content.Intent.ACTION_SEND).apply{
            type="text/plain";putExtra(android.content.Intent.EXTRA_TEXT,sb.toString());putExtra(android.content.Intent.EXTRA_SUBJECT,"Goat Farm Backup")
        }
        startActivity(android.content.Intent.createChooser(i,"Share backup"))
    }
}
