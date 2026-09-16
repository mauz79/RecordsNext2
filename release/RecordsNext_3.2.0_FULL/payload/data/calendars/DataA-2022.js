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
dataGiornata[1] = "August 14 2022"
dataGiornata[2] = "August 21 2022"
dataGiornata[3] = "August 28 2022"
dataGiornata[4] = "August 31 2022"
dataGiornata[5] = "September 4 2022"
dataGiornata[6] = "September 11 2022"
dataGiornata[7] = "September 18 2022"
dataGiornata[8] = "October 2 2022"
dataGiornata[9] = "October 9 2022"
dataGiornata[10] = "October 16 2022"
dataGiornata[11] = "October 23 2022"
dataGiornata[12] = "October 30 2022"
dataGiornata[13] = "November 6 2022"
dataGiornata[14] = "November 9 2022"
dataGiornata[15] = "November 13 2022"
dataGiornata[16] = "January 4 2023"
dataGiornata[17] = "January 8 2023"
dataGiornata[18] = "January 15 2023"
dataGiornata[19] = "January 22 2023"
dataGiornata[20] = "January 29 2023"
dataGiornata[21] = "February 5 2023"
dataGiornata[22] = "February 12 2023"
dataGiornata[23] = "February 19 2023"
dataGiornata[24] = "February 26 2023"
dataGiornata[25] = "March 5 2023"
dataGiornata[26] = "March 12 2023"
dataGiornata[27] = "March 19 2023"
dataGiornata[28] = "April 2 2023"
dataGiornata[29] = "April 8 2023"
dataGiornata[30] = "April 16 2023"
dataGiornata[31] = "April 23 2023"
dataGiornata[32] = "April 30 2023"
dataGiornata[33] = "May 3 2023"
dataGiornata[34] = "May 7 2023"
dataGiornata[35] = "May 14 2023"
dataGiornata[36] = "May 21 2023"
dataGiornata[37] = "May 28 2023"
dataGiornata[38] = "June 4 2023"

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