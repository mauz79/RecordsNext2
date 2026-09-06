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
dataGiornata[1] = "august 20 2016 18:00"
dataGiornata[2] = "august 27 2016 18:00"
dataGiornata[3] = "september 10 2016 18:00"
dataGiornata[4] = "september 16 2016 20:45"
dataGiornata[5] = "september 20 2016 20:45"
dataGiornata[6] = "september 24 2016 18:00"
dataGiornata[7] = "october 01 2016 18:00"
dataGiornata[8] = "october 15 2016 15:00"
dataGiornata[9] = "october 22 2016 18:00"
dataGiornata[10] = "october 25 2016 20:45"
dataGiornata[11] = "october 29 2016 18:00"
dataGiornata[12] = "november 05 2016 18:00"
dataGiornata[13] = "november 19 2016 15:00"
dataGiornata[14] = "november 26 2016 18:00"
dataGiornata[15] = "december 02 2016 20:45"
dataGiornata[16] = "december 10 2016 18:00"
dataGiornata[17] = "december 17 2016 15:00"
dataGiornata[18] = "december 20 2016 20:45"
dataGiornata[19] = "january 07 2017 18:00"
dataGiornata[20] = "january 14 2017 18:00"
dataGiornata[21] = "january 21 2017 18:00"
dataGiornata[22] = "january 28 2017 18:00"
dataGiornata[23] = "february 04 2017 18:00"
dataGiornata[24] = "february 11 2017 18:00"
dataGiornata[25] = "february 18 2017 18:00"
dataGiornata[26] = "february 25 2017 18:00"
dataGiornata[27] = "march 04 2017 18:00"
dataGiornata[28] = "march 11 2017 18:00"
dataGiornata[29] = "march 18 2017 18:00"
dataGiornata[30] = "april 01 2017 18:00"
dataGiornata[31] = "april 09 2017 18:00"
dataGiornata[32] = "april 14 2017 18:00"
dataGiornata[33] = "april 22 2017 18:00"
dataGiornata[34] = "april 29 2017 18:00"
dataGiornata[35] = "may 06 2017 18:00"
dataGiornata[36] = "may 13 2017 18:00"
dataGiornata[37] = "may 20 2017 18:00"
dataGiornata[38] = "may 27 2017 17:00"

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
