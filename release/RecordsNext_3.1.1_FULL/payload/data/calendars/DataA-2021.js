var dataGiornata = new Array()
// Immettere le date nel formato inglese: Mese Giorno Anno
// Gennaio = January
// Febbraio = February
// Marzo = March
// Aprile = April
// Maggio = May
// Giugno = June
// Luglio = July
// Agosto = August
// Settembre = September
// Ottobre = October
// Novembre = November
// Dicembre = December
dataGiornata[1] = "August 22 2021"
dataGiornata[2] = "August 29 2021"
dataGiornata[3] = "September 12 2021"
dataGiornata[4] = "September 19 2021"
dataGiornata[5] = "September 22 2021"
dataGiornata[6] = "September 26 2021"
dataGiornata[7] = "October 3 2021"
dataGiornata[8] = "October 17 2021"
dataGiornata[9] = "October 24 2021"
dataGiornata[10] = "October 27 2021"
dataGiornata[11] = "October 31 2021"
dataGiornata[12] = "November 7 2021"
dataGiornata[13] = "November 21 2021"
dataGiornata[14] = "November 28 2021"
dataGiornata[15] = "December 1 2021"
dataGiornata[16] = "December 5 2021"
dataGiornata[17] = "December 12 2021"
dataGiornata[18] = "December 19 2021"
dataGiornata[19] = "December 22 2021"
dataGiornata[20] = "January 6 2022"
dataGiornata[21] = "January 9 2022"
dataGiornata[22] = "January 16 2022"
dataGiornata[23] = "January 23 2022"
dataGiornata[24] = "February 6 2022"
dataGiornata[25] = "February 13 2022"
dataGiornata[26] = "February 20 2022"
dataGiornata[27] = "February 27 2022"
dataGiornata[28] = "March 6 2022"
dataGiornata[29] = "March 13 2022"
dataGiornata[30] = "March 20 2022"
dataGiornata[31] = "April 3 2022"
dataGiornata[32] = "April 10 2022"
dataGiornata[33] = "April 16 2022"
dataGiornata[34] = "April 24 2022"
dataGiornata[35] = "May 1 2022"
dataGiornata[36] = "May 8 2022"
dataGiornata[37] = "May 15 2022"
dataGiornata[38] = "May 22 2022"

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