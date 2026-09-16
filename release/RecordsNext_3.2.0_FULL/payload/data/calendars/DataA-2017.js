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
dataGiornata[1] = "August 20 2017"
dataGiornata[2] = "August 27 2017"
dataGiornata[3] = "September 10 2017"
dataGiornata[4] = "September 17 2017"
dataGiornata[5] = "September 20 2017"
dataGiornata[6] = "September 24 2017"
dataGiornata[7] = "October 1 2017"
dataGiornata[8] = "October 15 2017"
dataGiornata[9] = "October 22 2017"
dataGiornata[10] = "October 25 2017"
dataGiornata[11] = "October 29 2017"
dataGiornata[12] = "November 5 2017"
dataGiornata[13] = "November 19 2017"
dataGiornata[14] = "November 26 2017"
dataGiornata[15] = "December 3 2017"
dataGiornata[16] = "December 10 2017"
dataGiornata[17] = "December 17 2017"
dataGiornata[18] = "December 24 2017"
dataGiornata[19] = "December 31 2017"
dataGiornata[20] = "January 7 2018"
dataGiornata[21] = "January 21 2018"
dataGiornata[22] = "January 28 2018"
dataGiornata[23] = "February 4 2018"
dataGiornata[24] = "February 11 2018"
dataGiornata[25] = "February 18 2018"
dataGiornata[26] = "February 25 2018"
dataGiornata[27] = "March 4 2018"
dataGiornata[28] = "March 11 2018"
dataGiornata[29] = "March 18 2018"
dataGiornata[30] = "April 1 2018"
dataGiornata[31] = "April 8 2018"
dataGiornata[32] = "April 15 2018"
dataGiornata[33] = "April 18 2018"
dataGiornata[34] = "April 22 2018"
dataGiornata[35] = "April 29 2018"
dataGiornata[36] = "May 6 2018"
dataGiornata[37] = "May 13 2018"
dataGiornata[38] = "May 20 2018"

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
