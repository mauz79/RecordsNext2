/* Index bridge: no data recomputation; reads the RecordsNext plugin's published facade. */
(function(root,doc){'use strict';
 var card=doc.querySelector('[data-family-card="topFlop"]');if(!card)return;
 var badge=card.querySelector('.rn-availability'),link=card.querySelector('a');
 var f=root.fcmRecordsNextTopFlopViews;
 if(f&&f.schemaVersion==='4.0-public-views-v1'&&Array.isArray(f.stagioni)&&f.stagioni.length){
  badge.textContent='Disponibile · '+f.stagioni.length+' stagioni';badge.className='rn-availability is-ok';
  card.classList.remove('is-disabled');link.removeAttribute('aria-disabled');
 }else{
  badge.textContent='JS Top & Flop non disponibili';badge.className='rn-availability is-off';
  card.classList.add('is-disabled');link.setAttribute('aria-disabled','true');
  link.addEventListener('click',function(e){e.preventDefault()});
 }
})(window,document);
