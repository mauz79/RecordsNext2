var dataGiornata = new Array()
// Immettere le date nel formato inglese: Mese Giorno Anno
// Gennaio = January
// Febbraio = February
// Marzo = March
// Maprile = April
// Maggio = May
// Giugno = June
// Luglio = July
// Agosto = August
// Settembre = September
// Ottobre = October
// Novembre = November
// Dicembre = December
dataGiornata[1] = "august 25 2013"
dataGiornata[2] = "september 01 2013"
dataGiornata[3] = "september 15 2013"
dataGiornata[4] = "september 22 2013"
dataGiornata[5] = "september 25 2013"
dataGiornata[6] = "september 29 2013"
dataGiornata[7] = "october 06 2013"
dataGiornata[8] = "october 20 2013"
dataGiornata[9] = "october 27 2013"
dataGiornata[10] = "october 30 2013"
dataGiornata[11] = "november 03 2013"
dataGiornata[12] = "november 10 2013"
dataGiornata[13] = "november 24 2013"
dataGiornata[14] = "december 01 2013"
dataGiornata[15] = "december 08 2013"
dataGiornata[16] = "december 15 2013"
dataGiornata[17] = "december 22 2013"
dataGiornata[18] = "january 06 2014"
dataGiornata[19] = "january 12 2014"
dataGiornata[20] = "january 19 2014"
dataGiornata[21] = "january 26 2014"
dataGiornata[22] = "february 02 2014"
dataGiornata[23] = "february 09 2014"
dataGiornata[24] = "february 16 2014"
dataGiornata[25] = "february 23 2014"
dataGiornata[26] = "march 02 2014"
dataGiornata[27] = "march 09 2014"
dataGiornata[28] = "march 16 2014"
dataGiornata[29] = "march 23 2014"
dataGiornata[30] = "march 26 2014"
dataGiornata[31] = "march 30 2014"
dataGiornata[32] = "april 06 2014"
dataGiornata[33] = "april 13 2014"
dataGiornata[34] = "april 19 2014"
dataGiornata[35] = "april 27 2014"
dataGiornata[36] = "may 04 2014"
dataGiornata[37] = "may 11 2014"
dataGiornata[38] = "may 18 2014"

function initArray() {  
	this.length = initArray.arguments.length
    for (var i = 0; i < this.length; i++)
    this[i+1] = initArray.arguments[i]
}
var DOWArray = new initArray("Dom","Lun","Mar","Mer","Gio","Ven","Sab")
var MOYArray = new initArray("Gen","Feb","Mar","Apr","Mag","Giu","Lug","Ago","Set","Ott","Nov","Dic")
var Year
//for (t = 1; t < dataGiornata.length-1 ;t++ ) {
for (t = 1; t < dataGiornata.length ;t++ ) {
	data = new Date(dataGiornata[t])
	Year = data.getYear()
	if (Year < 2000)
		Year = Year + 1900
	dataGiornata[t] = DOWArray[(data.getDay()+1)] + " " + data.getDate() + " " + MOYArray[(data.getMonth()+1)] + " " + Year
}