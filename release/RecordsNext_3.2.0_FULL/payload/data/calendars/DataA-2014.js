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
dataGiornata[1] = "august 31 2014"
dataGiornata[2] = "september 14 2014"
dataGiornata[3] = "september 21 2014"
dataGiornata[4] = "september 24 2014"
dataGiornata[5] = "september 28 2014"
dataGiornata[6] = "october 05 2014"
dataGiornata[7] = "october 19 2014"
dataGiornata[8] = "october 26 2014"
dataGiornata[9] = "october 29 2014"
dataGiornata[10] = "november 02 2014"
dataGiornata[11] = "november 09 2014"
dataGiornata[12] = "november 23 2014"
dataGiornata[13] = "november 30 2014"
dataGiornata[14] = "december 07 2014"
dataGiornata[15] = "december 14 2014"
dataGiornata[16] = "december 21 2014"
dataGiornata[17] = "january 06 2015"
dataGiornata[18] = "january 11 2015"
dataGiornata[19] = "january 18 2015"
dataGiornata[20] = "january 25 2015"
dataGiornata[21] = "february 01 2015"
dataGiornata[22] = "february 08 2015"
dataGiornata[23] = "february 15 2015"
dataGiornata[24] = "february 22 2015"
dataGiornata[25] = "march 01 2015"
dataGiornata[26] = "march 08 2015"
dataGiornata[27] = "march 15 2015"
dataGiornata[28] = "march 22 2015"
dataGiornata[29] = "april 04 2015"
dataGiornata[30] = "april 12 2015"
dataGiornata[31] = "april 19 2015"
dataGiornata[32] = "april 26 2015"
dataGiornata[33] = "april 29 2015"
dataGiornata[34] = "may 03 2015"
dataGiornata[35] = "may 10 2015"
dataGiornata[36] = "may 17 2015"
dataGiornata[37] = "may 24 2015"
dataGiornata[38] = "may 31 2015"

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