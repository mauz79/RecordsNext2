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
dataGiornata[1] = "August 25 2019"
dataGiornata[2] = "September 1 2019"
dataGiornata[3] = "September 15 2019"
dataGiornata[4] = "September 22 2019"
dataGiornata[5] = "September 25 2019"
dataGiornata[6] = "September 29 2019"
dataGiornata[7] = "October 6 2019"
dataGiornata[8] = "October 20 2019"
dataGiornata[9] = "October 27 2019"
dataGiornata[10] = "October 30 2019"
dataGiornata[11] = "November 3 2019"
dataGiornata[12] = "November 10 2019"
dataGiornata[13] = "November 24 2019"
dataGiornata[14] = "December 1 2019"
dataGiornata[15] = "December 8 2019"
dataGiornata[16] = "December 15 2019"
dataGiornata[17] = "December 22 2019"
dataGiornata[18] = "January 5 2020"
dataGiornata[19] = "January 12 2020"
dataGiornata[20] = "January 19 2020"
dataGiornata[21] = "January 26 2020"
dataGiornata[22] = "February 2 2020"
dataGiornata[23] = "February 9 2020"
dataGiornata[24] = "February 16 2020"
dataGiornata[25] = "February 23 2020"
dataGiornata[26] = "March 1 2020"
dataGiornata[27] = "March 8 2020"
dataGiornata[28] = "March 15 2020"
dataGiornata[29] = "March 22 2020"
dataGiornata[30] = "April 5 2020"
dataGiornata[31] = "April 11 2020"
dataGiornata[32] = "April 19 2020"
dataGiornata[33] = "April 22 2020"
dataGiornata[34] = "April 26 2020"
dataGiornata[35] = "May 3 2020"
dataGiornata[36] = "May 10 2020"
dataGiornata[37] = "May 17 2020"
dataGiornata[38] = "May 24 2020"

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
