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
dataGiornata[1] = "august 26 2012"
dataGiornata[2] = "september 02 2012"
dataGiornata[3] = "september 16 2012"
dataGiornata[4] = "september 23 2012"
dataGiornata[5] = "september 26 2012"
dataGiornata[6] = "september 30 2012"
dataGiornata[7] = "october 07 2012"
dataGiornata[8] = "october 21 2012"
dataGiornata[9] = "october 28 2012"
dataGiornata[10] = "october 31 2012"
dataGiornata[11] = "november 04 2012"
dataGiornata[12] = "november 11 2012"
dataGiornata[13] = "november 18 2012"
dataGiornata[14] = "november 25 2012"
dataGiornata[15] = "december 02 2012"
dataGiornata[16] = "december 09 2012"
dataGiornata[17] = "december 16 2012"
dataGiornata[18] = "december 22 2012"
dataGiornata[19] = "january 06 2013"
dataGiornata[20] = "january 13 2013"
dataGiornata[21] = "january 20 2013"
dataGiornata[22] = "january 27 2013"
dataGiornata[23] = "february 03 2013"
dataGiornata[24] = "february 10 2013"
dataGiornata[25] = "february 17 2013"
dataGiornata[26] = "february 24 2013"
dataGiornata[27] = "march 03 2013"
dataGiornata[28] = "march 10 2013"
dataGiornata[29] = "march 17 2013"
dataGiornata[30] = "march 30 2013"
dataGiornata[31] = "april 07 2013"
dataGiornata[32] = "april 14 2013"
dataGiornata[33] = "april 21 2013"
dataGiornata[34] = "april 28 2013"
dataGiornata[35] = "may 05 2013"
dataGiornata[36] = "may 08 2013"
dataGiornata[37] = "may 12 2013"
dataGiornata[38] = "may 19 2013"

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