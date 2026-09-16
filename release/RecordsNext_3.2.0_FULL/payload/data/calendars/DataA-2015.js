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
dataGiornata[1] = "august 23 2015"
dataGiornata[2] = "august 30 2015"
dataGiornata[3] = "september 13 2015"
dataGiornata[4] = "september 20 2015"
dataGiornata[5] = "september 23 2015"
dataGiornata[6] = "september 27 2015"
dataGiornata[7] = "october 4 2015"
dataGiornata[8] = "october 18 2015"
dataGiornata[9] = "october 25 2015"
dataGiornata[10] = "october 28 2015"
dataGiornata[11] = "november 1 2015"
dataGiornata[12] = "november 8 2015"
dataGiornata[13] = "november 22 2015"
dataGiornata[14] = "november 29 2015"
dataGiornata[15] = "december 6 2015"
dataGiornata[16] = "december 13 2015"
dataGiornata[17] = "december 20 2015"
dataGiornata[18] = "january 6 2016"
dataGiornata[19] = "january 10 2016"
dataGiornata[20] = "january 17 2016"
dataGiornata[21] = "january 24 2016"
dataGiornata[22] = "january 31 2016"
dataGiornata[23] = "february 3 2016"
dataGiornata[24] = "february 7 2016"
dataGiornata[25] = "february 14 2016"
dataGiornata[26] = "february 21 2016"
dataGiornata[27] = "february 28 2016"
dataGiornata[28] = "march 6 2016"
dataGiornata[29] = "march 13 2016"
dataGiornata[30] = "march 20 2016"
dataGiornata[31] = "april 3 2016"
dataGiornata[32] = "april 10 2016"
dataGiornata[33] = "april 17 2016"
dataGiornata[34] = "april 20 2016"
dataGiornata[35] = "april 24 2016"
dataGiornata[36] = "may 1 2016"
dataGiornata[37] = "may 8 2016"
dataGiornata[38] = "may 15 2016"

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
