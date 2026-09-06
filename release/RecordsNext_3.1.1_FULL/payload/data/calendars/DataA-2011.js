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
dataGiornata[1] = "august 27 2011"
dataGiornata[2] = "september 10 2011"
dataGiornata[3] = "september 17 2011"
dataGiornata[4] = "september 21 2011"
dataGiornata[5] = "september 24 2011"
dataGiornata[6] = "october 01 2011"
dataGiornata[7] = "october 15 2011"
dataGiornata[8] = "october 22 2011"
dataGiornata[9] = "october 26 2011"
dataGiornata[10] = "october 29 2011"
dataGiornata[11] = "november 5 2011"
dataGiornata[12] = "november 19 2011"
dataGiornata[13] = "november 26 2011"
dataGiornata[14] = "december 03 2011"
dataGiornata[15] = "december 10 2011"
dataGiornata[16] = "december 17 2011"
dataGiornata[17] = "january 07 2012"
dataGiornata[18] = "january 14 2012"
dataGiornata[19] = "january 21 2012"
dataGiornata[20] = "january 28 2012"
dataGiornata[21] = "february 01 2012"
dataGiornata[22] = "february 04 2012"
dataGiornata[23] = "february 11 2012"
dataGiornata[24] = "february 18 2012"
dataGiornata[25] = "february 25 2012"
dataGiornata[26] = "march 03 2012"
dataGiornata[27] = "march 10 2012"
dataGiornata[28] = "march 17 2012"
dataGiornata[29] = "march 24 2012"
dataGiornata[30] = "march 31 2012"
dataGiornata[31] = "april 07 2012"
dataGiornata[32] = "april 11 2012"
dataGiornata[33] = "april 14 2012"
dataGiornata[34] = "april 21 2012"
dataGiornata[35] = "april 28 2012"
dataGiornata[36] = "may 02 2012"
dataGiornata[37] = "may 05 2012"
dataGiornata[38] = "may 13 2012"

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