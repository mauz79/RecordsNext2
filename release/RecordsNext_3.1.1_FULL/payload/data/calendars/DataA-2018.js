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
dataGiornata[1] = "August 19 2018"
dataGiornata[2] = "August 26 2018"
dataGiornata[3] = "September 2 2018"
dataGiornata[4] = "September 16 2018"
dataGiornata[5] = "September 23 2018"
dataGiornata[6] = "September 26 2018"
dataGiornata[7] = "September 30 2018"
dataGiornata[8] = "October 7 2018"
dataGiornata[9] = "October 21 2018"
dataGiornata[10] = "October 28 2018"
dataGiornata[11] = "November 4 2018"
dataGiornata[12] = "November 11 2018"
dataGiornata[13] = "November 25 2018"
dataGiornata[14] = "December 2 2018"
dataGiornata[15] = "December 9 2018"
dataGiornata[16] = "December 16 2018"
dataGiornata[17] = "December 22 2018"
dataGiornata[18] = "December 26 2018"
dataGiornata[19] = "December 29 2018"
dataGiornata[20] = "January 20 2019"
dataGiornata[21] = "January 27 2019"
dataGiornata[22] = "February 3 2019"
dataGiornata[23] = "February 10 2019"
dataGiornata[24] = "February 17 2019"
dataGiornata[25] = "February 24 2019"
dataGiornata[26] = "March 3 2019"
dataGiornata[27] = "March 10 2019"
dataGiornata[28] = "March 17 2019"
dataGiornata[29] = "March 31 2019"
dataGiornata[30] = "April 3 2019"
dataGiornata[31] = "April 7 2019"
dataGiornata[32] = "April 14 2019"
dataGiornata[33] = "April 20 2019"
dataGiornata[34] = "April 28 2019"
dataGiornata[35] = "May 5 2019"
dataGiornata[36] = "May 12 2019"
dataGiornata[37] = "May 19 2019"
dataGiornata[38] = "May 26 2019"

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
